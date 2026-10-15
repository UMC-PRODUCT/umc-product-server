import { durationMs, positiveInteger, readChatConfig } from "./chat.js";

function readSenders(env, users) {
  if (env.CHAT_SENDERS === undefined) return Array.from({ length: users }, (_, index) => index + 1);
  let senders;
  try { senders = JSON.parse(env.CHAT_SENDERS); } catch (_) {
    throw new Error("CHAT_SENDERS는 [1,3] 형식의 JSON이어야 합니다");
  }
  if (!Array.isArray(senders) || !senders.length || new Set(senders).size !== senders.length
      || senders.some((index) => !Number.isInteger(index) || index < 1 || index > users)) {
    throw new Error("CHAT_SENDERS에는 메시지를 보낼 사용자 번호를 1~CHAT_USERS 사이에서 중복 없이 지정하세요");
  }
  return senders;
}

function readVuUsers(env, users) {
  if (env.CHAT_VU_USERS !== undefined) {
    if (env.CHAT_USERS_PER_VU !== undefined) {
      throw new Error("CHAT_VU_USERS와 CHAT_USERS_PER_VU는 하나만 지정하세요");
    }
    let groups;
    try { groups = JSON.parse(env.CHAT_VU_USERS); } catch (_) {
      throw new Error("CHAT_VU_USERS는 [[1,3],[2,4]] 형식의 JSON이어야 합니다");
    }
    if (!Array.isArray(groups) || !groups.length || groups.some((group) => !Array.isArray(group)
        || !group.length || group.some((index) => !Number.isInteger(index) || index < 1 || index > users))) {
      throw new Error("CHAT_VU_USERS에는 1~CHAT_USERS 사이 사용자 번호를 빈 그룹 없이 지정하세요");
    }
    const indices = groups.flat();
    if (indices.length !== users || new Set(indices).size !== users) {
      throw new Error("CHAT_VU_USERS는 모든 사용자를 중복 없이 정확히 한 번 배정해야 합니다");
    }
    return groups;
  }
  const perVu = positiveInteger(env.CHAT_USERS_PER_VU, 1, "CHAT_USERS_PER_VU", 1, 1000);
  // 기본 시나리오의 배정 정책이다. 나누어떨어지지 않으면 마지막 VU가 남은 사용자를 담당한다.
  return Array.from({ length: Math.ceil(users / perVu) }, (_, group) =>
    Array.from({ length: Math.min(perVu, users - group * perVu) }, (_, index) => group * perVu + index + 1));
}

export function readLoadConfig(env) {
  const users = positiveInteger(env.CHAT_USERS, 2, "CHAT_USERS", 1, 1000);
  return { ...readChatConfig(env), users, vuUsers: readVuUsers(env, users), senders: readSenders(env, users),
    rooms: positiveInteger(env.CHAT_ROOMS, 1, "CHAT_ROOMS", 1, 1000),
    intervalMs: positiveInteger(env.CHAT_MESSAGE_INTERVAL_MS, 1000, "CHAT_MESSAGE_INTERVAL_MS", 1, 60000),
    startupMs: positiveInteger(env.CHAT_STARTUP_TIMEOUT_MS, 10000, "CHAT_STARTUP_TIMEOUT_MS", 100, 120000),
    durationMs: durationMs(env.DURATION || "30s"),
    tags: { scenario: "chat-message", profile: "load" } };
}

export function buildLoadOptions(config) {
  const vus = config.vuUsers.length;
  return {
    // 반복 횟수는 VU 수를 기준으로 검사하고 실제 연결 수는 ws_sessions에서 별도로 확인한다.
    scenarios: { chat_load: { executor: "per-vu-iterations", vus, iterations: 1,
      maxDuration: `${config.startupMs + config.durationMs + config.timeoutMs * 3 + 3000}ms` } },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "count"],
    thresholds: { checks: ["rate==1"], iterations: [`count==${vus}`],
      umc_chat_failures: ["count==0"], umc_chat_delivery_missing: ["count==0"],
      umc_chat_completion_missing: ["count==0"] },
  };
}
