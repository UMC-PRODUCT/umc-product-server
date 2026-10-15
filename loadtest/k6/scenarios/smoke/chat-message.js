import { check } from "k6";

import { buildPairOptions, readPairConfig } from "../../config/chat-pair.js";
import { runChatPair, selectPair } from "../helpers/chat-pair.js";
import { initializeMetrics } from "../../lib/chat/chat-metrics.js";

export const requiresSeed = true;

// 선택된 시나리오의 설정만 읽어 다른 HTTP 테스트에 Chat 환경변수 검증이 영향을 주지 않게 한다.
export function getOptions() {
  return buildPairOptions(readPairConfig(__ENV));
}

export function validateSeed(seed) {
  selectPair(seed, readPairConfig(__ENV));
}

export default async function (seed) {
  const config = readPairConfig(__ENV);
  const target = selectPair(seed, config);
  initializeMetrics(config.tags);
  const { session, failure } = await runChatPair(target, config, async (chat) => {
    const message = await chat.sendMessage({ phase: "Smoke" });
    console.log(`Chat Smoke 송수신 성공: threadId=${target.threadId}, messageId=${message.messageId}`);
  });
  check(session, {
    "두 회원 STOMP 인증 완료": (value) => value.connected,
    "준비 메시지로 구독 확인": (value) => value.ready,
    "발신자 ACK 일치": (value) => value.lastResult.acknowledged === true,
    "상대방 메시지 수신 일치": (value) => value.lastResult.delivered === true,
    "ACK와 수신 메시지 ID 일치": (value) => value.lastResult.correlated === true,
    "모든 연결 종료": (value) => value.closed,
    "Smoke 오류 없음": () => failure === null,
  }, config.tags);
  if (failure) throw failure;
}
