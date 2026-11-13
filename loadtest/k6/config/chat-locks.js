import { durationMs, positiveInteger, readChatConfig } from "./chat.js";

export class LockSuiteError extends Error {
  constructor(message) { super(message); this.name = "LockSuiteError"; }
}

export const lockOperations = ["reaction", "read", "join", "report"];

export function readLockConfig(env) {
  const operation = env.CHAT_LOCK_OPERATION || "reaction";
  if (![...lockOperations, "mixed"].includes(operation)) throw new LockSuiteError("지원하지 않는 락 측정입니다");
  const users = positiveInteger(env.CHAT_USERS, 10, "CHAT_USERS", 10, 50);
  if (![10, 50].includes(users)) throw new LockSuiteError("CHAT_USERS는 10 또는 50입니다");
  const warmupMs = env.CHAT_WARMUP === "0s" ? 0 : durationMs(env.CHAT_WARMUP || "30s");
  const measurementMs = durationMs(env.DURATION || "2m");
  const intervalMs = positiveInteger(env.CHAT_LOCK_INTERVAL_MS, 2500, "CHAT_LOCK_INTERVAL_MS", 500, 60000);
  if (warmupMs % intervalMs || measurementMs % intervalMs) {
    throw new LockSuiteError("워밍업과 측정 시간은 요청 주기의 배수여야 합니다");
  }
  if ((warmupMs + measurementMs) / intervalMs > 120) throw new LockSuiteError("한 측정은 최대 120회차까지 지원합니다");
  const roles = Array.from({ length: users }, (_, index) => operation === "mixed"
    ? lockOperations[index % lockOperations.length] : operation);
  return { ...readChatConfig(env), operation, users, roles, warmupMs, measurementMs, intervalMs,
    warmupRounds: warmupMs / intervalMs, measurementRounds: measurementMs / intervalMs,
    startupMs: positiveInteger(env.CHAT_STARTUP_TIMEOUT_MS, 30000, "CHAT_STARTUP_TIMEOUT_MS", 1000, 120000),
    maxInflight: positiveInteger(env.CHAT_MAX_INFLIGHT, 32, "CHAT_MAX_INFLIGHT", 1, 128),
    tags: { scenario: "chat-locks", workload: operation } };
}

export function lockSeed(seed, config) {
  const id = (value) => typeof value === "string" && /^[1-9][0-9]*$/.test(value);
  if (seed?.baseUrl !== config.baseUrl || !Array.isArray(seed.memberIds)
      || seed.memberIds.length !== config.users * 2 + 1 || !seed.memberIds.every(id)
      || new Set(seed.memberIds).size !== seed.memberIds.length || seed.rooms?.length !== 0) {
    throw new LockSuiteError("회원만 준비한 2 × CHAT_USERS + 1명 시드가 필요합니다");
  }
  return { actors: seed.memberIds.slice(0, config.users), author: seed.memberIds[config.users],
    invitees: seed.memberIds.slice(config.users + 1) };
}

export function lockOptions(config) {
  const seconds = Math.ceil((config.startupMs + config.warmupMs + config.measurementMs
    + config.timeoutMs * 6 + 10000) / 1000);
  const thresholds = {
    umc_chat_lock_run_failures: ["count==0"], umc_chat_lock_failures: ["count==0"],
    umc_chat_lock_noops: ["count==0"], umc_chat_lock_invalid_timing: ["count==0"],
    umc_chat_schedule_dropped: ["count==0"], iterations: ["count==" + config.users], checks: ["rate==1"],
  };
  for (const role of new Set(config.roles)) {
    for (const operation of role === "reaction" ? ["reaction_add", "reaction_remove"] : [role]) {
      const count = config.roles.filter((item) => item === role).length * config.measurementRounds;
      thresholds[`umc_chat_lock_successes{operation:${operation}}`] = ["count==" + count];
      thresholds[`umc_chat_lock_latency_ms{operation:${operation}}`] = [];
      thresholds[`umc_chat_lock_error_rate{operation:${operation}}`] = ["rate==0"];
    }
  }
  return { setupTimeout: "30m", scenarios: { locks: { executor: "per-vu-iterations",
    vus: config.users, iterations: 1, maxDuration: seconds + "s" } },
  summaryTrendStats: ["avg", "med", "p(95)", "p(99)", "max", "count"], thresholds };
}
