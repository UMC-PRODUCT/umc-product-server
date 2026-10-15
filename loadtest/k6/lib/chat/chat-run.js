import { readLoadConfig } from "../../config/chat-load.js";
import { readPairConfig } from "../../config/chat-pair.js";

// 원본 환경변수 대신 기본값과 사용자 배정까지 계산된 설정을 저장한다. 인증 토큰은 포함하지 않는다.
export function chatRunConditions(profile, env, seed, options) {
  const config = profile === "smoke" ? readPairConfig(env) : readLoadConfig(env);
  const rooms = profile === "smoke" ? seed.rooms.slice(0, 1) : seed.rooms;
  return {
    profile, scenario: "chat-message", config, k6Options: options,
    rooms: rooms.map((room) => ({ threadId: room.threadId, memberIds: room.memberIds })),
    vuMemberIds: profile === "smoke" ? [rooms[0].memberIds]
      : config.vuUsers.map((group) => group.map((index) => seed.memberIds[index - 1])),
    senderMemberIds: profile === "smoke" ? [rooms[0].memberIds[0]]
      : config.senders.map((index) => seed.memberIds[index - 1]),
    loadModel: profile === "smoke" ? "한 번 송수신" : "사용자별 ACK 대기 후 다음 전송",
  };
}
