import { vu } from "k6/execution";

import { buildLoadOptions, readLoadConfig } from "../../config/chat-load.js";
import { runChatUsers } from "../../lib/chat/chat-load-session.js";
import { createLoadPlan } from "../../lib/chat/chat-load.js";
import { repeatMessages, receiveMessages } from "../helpers/chat-message-behavior.js";

export const requiresSeed = true;
export const getOptions = () => buildLoadOptions(readLoadConfig(__ENV));
export const validateSeed = (seed) => createLoadPlan(seed, readLoadConfig(__ENV));

export function prepareRun() {
  const config = readLoadConfig(__ENV);
  // setup에서 한 번 만든 값을 모든 VU가 복사해 받는다. VU끼리 JS 객체를 공유하지 않는다.
  const startAt = Date.now() + config.startupMs;
  return { runId: crypto.randomUUID(), startAt, endAt: startAt + config.durationMs,
    drainUntil: startAt + config.durationMs + config.timeoutMs * 3 };
}

export default async function (seed, run) {
  const config = readLoadConfig(__ENV);
  const users = createLoadPlan(seed, config);
  const indices = config.vuUsers[vu.idInTest - 1];
  if (!indices) throw new Error("현재 VU에 배정할 사용자가 없습니다");
  // 사용자 번호는 시드 memberIds의 1부터 시작하는 순서다. 방 참여 순서와 VU 배정은 무관하다.
  const assigned = indices.map((index) => users[index - 1]);
  const senders = new Set(config.senders.map((index) => users[index - 1].memberId));
  // 다른 시나리오도 원하는 사용자 목록을 구성해 같은 송수신·측정 기능을 재사용할 수 있다.
  return runChatUsers(assigned, config, run, (target) => senders.has(target.memberId)
    ? repeatMessages(config.intervalMs) : receiveMessages());
}
