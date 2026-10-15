import { readChatConfig } from "./chat.js";

export function readPairConfig(env) {
  const connection = readChatConfig(env);
  // 두 사용자·한 방·한 번 송수신은 Smoke의 최소 구성이다. 반복 부하의 사용자 배정과 분리한다.
  return { ...connection, tags: { scenario: "chat-message", profile: "smoke" } };
}

export function buildPairOptions(config) {
  // 인증·연결 준비부터 메시지 송수신·연결 종료까지 마칠 수 있도록 전체 실행 제한 시간에 여유를 둔다.
  const overhead = config.timeoutMs * 4 + Math.min(config.timeoutMs, 3000) * 3 + 3000;
  return {
    scenarios: {
      chat_smoke: { executor: "per-vu-iterations", vus: 1,
        iterations: 1, maxDuration: `${overhead}ms` },
    },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "count"],
    thresholds: { checks: ["rate==1"], iterations: ["count==1"],
      umc_chat_failures: ["count==0"] },
  };
}
