import { durationMs, positiveInteger, readChatConfig } from "./chat.js";

export class ReactionConfigError extends Error {
  constructor(message) { super(message); this.name = "ReactionConfigError"; }
}

export function readReactionConfig(env) {
  const mode = env.CHAT_REACTION_MODE || "load";
  if (!["load", "duplicate"].includes(mode)) throw new ReactionConfigError("CHAT_REACTION_MODE는 load 또는 duplicate입니다");
  const warmup = env.CHAT_REACTION_WARMUP || "30s";
  const variant = env.CHAT_LOCK_VARIANT || "baseline";
  if (!["baseline", "removed"].includes(variant)) throw new ReactionConfigError("CHAT_LOCK_VARIANT는 기록용 baseline 또는 removed입니다");
  return {
    ...readChatConfig(env), mode, variant, serverRevision: env.CHAT_SERVER_REVISION || "unverified",
    users: mode === "duplicate" ? 1 : positiveInteger(env.CHAT_USERS, 10, "CHAT_USERS", 1, 100),
    emoji: env.CHAT_REACTION_EMOJI || "👍",
    intervalMs: positiveInteger(env.CHAT_REACTION_INTERVAL_MS, 1000, "CHAT_REACTION_INTERVAL_MS",
      mode === "duplicate" ? 250 : 100, 60000),
    startupMs: positiveInteger(env.CHAT_STARTUP_TIMEOUT_MS, 30000, "CHAT_STARTUP_TIMEOUT_MS", 100, 120000),
    warmupMs: warmup === "0s" ? 0 : durationMs(warmup),
    durationMs: durationMs(env.DURATION || "2m"),
    rounds: positiveInteger(env.CHAT_REACTION_ROUNDS, 10, "CHAT_REACTION_ROUNDS", 1, 100),
    tags: { scenario: "chat-reaction", profile: "load", mode },
  };
}

export function createReactionPlan(input, config) {
  const seed = input?.reactionSeed || input;
  const id = (value) => typeof value === "string" && /^[1-9][0-9]*$/.test(value);
  if (seed?.baseUrl !== config.baseUrl || !Array.isArray(seed.memberIds)
      || !seed.memberIds.length || !seed.memberIds.every(id)
      || new Set(seed.memberIds).size !== seed.memberIds.length
      || !Array.isArray(seed.rooms) || seed.rooms.length !== 1) {
    throw new ReactionConfigError("BASE_URL이 일치하는 한 방의 회원 시드가 필요합니다");
  }
  const room = seed.rooms[0];
  if (!id(room?.threadId) || !Array.isArray(room.memberIds)
      || room.memberIds.length !== seed.memberIds.length
      || new Set(room.memberIds).size !== room.memberIds.length
      || !room.memberIds.every((member) => seed.memberIds.includes(member))) {
    throw new ReactionConfigError("모든 시드 회원이 같은 방에 참여해야 합니다");
  }
  if (config.mode === "load" && seed.memberIds.length !== config.users) {
    throw new ReactionConfigError("CHAT_USERS와 시드의 전체 회원 수가 일치해야 합니다");
  }
  if (room.messageIds !== undefined && (!Array.isArray(room.messageIds)
      || room.messageIds.length < config.users || room.messageIds.length > seed.memberIds.length || !room.messageIds.every(id)
      || new Set(room.messageIds).size !== room.messageIds.length)) {
    throw new ReactionConfigError("messageIds는 memberIds 순서로 배정한 서로 다른 메시지 ID여야 합니다");
  }
  return { threadId: room.threadId, memberIds: seed.memberIds,
    targets: seed.memberIds.slice(0, config.users).map((memberId, index) => ({
      memberId, threadId: room.threadId, messageId: room.messageIds?.[index] || null,
    })) };
}

export function buildReactionOptions(config) {
  const runMs = config.mode === "duplicate" ? config.rounds * (config.timeoutMs * 4 + config.intervalMs)
    : config.startupMs + config.warmupMs + config.durationMs;
  return {
    setupTimeout: "5m",
    scenarios: { reactions: { executor: "per-vu-iterations", vus: config.users, iterations: 1,
      maxDuration: String(runMs + config.timeoutMs * 6 + 5000) + "ms" } },
    summaryTrendStats: ["avg", "min", "med", "max", "p(95)", "p(99)", "count"],
    thresholds: { checks: ["rate==1"], iterations: ["count==" + config.users],
      umc_chat_reaction_failures: ["count==0"], umc_chat_reaction_error_rate: ["rate==0"],
      "umc_chat_reaction_ack_latency_ms{operation:add}": [],
      "umc_chat_reaction_ack_latency_ms{operation:remove}": [],
      umc_chat_reaction_ready_users: ["count==" + config.users] },
  };
}
