import { scenario } from "k6/execution";

import { buildLoadOptions, readLoadConfig } from "../../config/chat-load.js";
import { runChatUsers } from "../../lib/chat/chat-load-session.js";
import { createLoadPlan } from "../../lib/chat/chat-load.js";
import { fixedMessages, receiveMessages } from "../helpers/chat-message-behavior.js";

export const requiresSeed = true;
export const getOptions = () => buildLoadOptions(readLoadConfig(__ENV));
export const validateSeed = (seed) => createLoadPlan(seed, readLoadConfig(__ENV));

// 이 시나리오의 실제 배정과 부하 모델을 기록한다. 새 행동에 설정을 추가하면 여기에도 함께 남긴다.
export function getRunConditions(seed, context, summary) {
  const config = readLoadConfig(__ENV);
  createLoadPlan(seed, config);
  return {
    config,
    rooms: seed.rooms.map((room) => ({ threadId: room.threadId, memberIds: room.memberIds })),
    vuMemberIds: config.vuUsers.map((group) => group.map((index) => seed.memberIds[index - 1])),
    senderMemberIds: config.senders.map((index) => seed.memberIds[index - 1]),
    loadModel: "ACK와 독립적인 일정 주기 전송",
    variantIsLabelOnly: true,
    preparation: { protocol: "room-leader-v1",
      requests: summary?.metrics?.umc_chat_preparation_requests?.values?.count ?? null,
      readyUsers: summary?.metrics?.umc_chat_preparation_ready?.values?.count ?? null },
    measurement: context ? { startAt: context.measureAt, endAt: context.endAt } : null,
    rates: { messageTargetPerSecond: config.senders.length * 1000 / config.intervalMs },
    measuredResults: summary ? measuredResults(summary, config) : null,
  };
}

export function prepareRun() {
  const config = readLoadConfig(__ENV);
  // setup에서 한 번 만든 값을 모든 VU가 복사해 받는다. VU끼리 JS 객체를 공유하지 않는다.
  const startAt = Date.now() + config.startupMs;
  return { runId: crypto.randomUUID(), startAt, measureAt: startAt + config.warmupMs, endAt: startAt + config.warmupMs + config.durationMs,
    drainUntil: startAt + config.warmupMs + config.durationMs + config.timeoutMs * 3 };
}

export default async function (seed, run) {
  const config = readLoadConfig(__ENV);
  const users = createLoadPlan(seed, config);
  const indices = config.vuUsers[scenario.iterationInTest];
  if (!indices) throw new Error("현재 VU에 배정할 사용자가 없습니다");
  // 사용자 번호는 시드 memberIds의 1부터 시작하는 순서다. 방 참여 순서와 VU 배정은 무관하다.
  const assigned = indices.map((index) => users[index - 1]);
  const senders = new Set(config.senders.map((index) => users[index - 1].memberId));
  // 다른 시나리오도 원하는 사용자 목록을 구성해 같은 송수신·측정 기능을 재사용할 수 있다.
  return runChatUsers(assigned, config, run, (target) => {
    const senderIndex = config.senders.findIndex((index) => users[index - 1].memberId === target.memberId);
    return senders.has(target.memberId)
      ? fixedMessages(Math.floor(senderIndex * config.intervalMs / config.senders.length),
        users.findIndex((user) => user.memberId === target.memberId) + 1)
      : receiveMessages();
  });
}

function measuredResults(summary, config) {
  const count = (name) => summary.metrics?.[name]?.values?.count ?? null;
  const attempts = count("umc_chat_message_attempts");
  const successes = count("umc_chat_ack_successes");
  const transmitted = count("umc_chat_message_transmitted");
  const seconds = config.durationMs / 1000;
  return {
    scheduled: count("umc_chat_scheduled{phase:measurement}"),
    issued: count("umc_chat_issued{phase:measurement}"),
    dropped: count("umc_chat_schedule_dropped{phase:measurement}"),
    attempts, transmitted, successes,
    transmittedPerSecond: transmitted === null ? null : transmitted / seconds,
    attemptedPerSecond: attempts === null ? null : attempts / seconds,
    acknowledgedPerSecond: successes === null ? null : successes / seconds };
}
