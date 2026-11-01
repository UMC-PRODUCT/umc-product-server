import { check } from "k6";

import { ChatClient } from "./chat-client.js";
import { initializeLoadMetrics, recordAckResult, recordDelivery, recordDeliverySummary,
  recordFailure } from "./chat-metrics.js";
import { DeliveryTracker, loadContent } from "./chat-load.js";

/**
 * 현재 VU에 배정된 사용자들을 동시에 실행하고, 각 사용자의 연결·준비 확인·측정·종료를 관리한다.
 * 시나리오에서 targets에 사용자와 참여 방을, run에 setup에서 만든 공통 실행 시각·runId를 넘긴다.
 * behaviorForUser(target)는 { act, checks }를 반환한다. act에는 sendMessage·pause·waitUntilEnd 등이 전달된다.
 * 새 시나리오는 이 행동과 성공 조건을 바꿔 재사용한다. act의 반환값은 checks에서 state.actionResult로 읽는다.
 * 한 사용자가 실패해도 나머지 사용자의 연결 정리가 끝날 때까지 기다린 뒤 오류를 던진다.
 */
export async function runChatUsers(targets, config, run, behaviorForUser) {
  if (!Array.isArray(targets) || !targets.length
      || new Set(targets.map((target) => target.memberId)).size !== targets.length) {
    throw new Error("한 VU에 배정할 서로 다른 사용자 목록이 필요합니다");
  }
  if (!run?.runId || !Number.isFinite(run.startAt) || !Number.isFinite(run.endAt)
      || !Number.isFinite(run.drainUntil)) throw new Error("setup의 공통 실행 정보가 필요합니다");
  if (typeof behaviorForUser !== "function") throw new Error("사용자별 행동을 정하는 함수가 필요합니다");
  const plans = targets.map((target) => {
    const behavior = behaviorForUser(target);
    const checks = behavior?.checks || {};
    if (typeof behavior?.act !== "function" || typeof checks !== "object" || Array.isArray(checks)
        || Object.values(checks).some((predicate) => typeof predicate !== "function")) {
      throw new Error("사용자 행동에는 act 함수와 성공 조건 checks가 필요합니다");
    }
    return { target, behavior: { ...behavior, checks } };
  });
  // 행동과 성공 조건은 시나리오가 정하고, 공통 실행부는 모든 사용자의 연결·측정·종료를 관리한다.
  const results = await Promise.allSettled(plans.map(({ target, behavior }) => runChatUser(target, config, run, behavior)));
  // 한 사용자가 실패해도 나머지 사용자의 종료까지 기다려 연결·타이머가 남지 않게 한다.
  const failed = results.find((result) => result.status === "rejected");
  if (failed) throw failed.reason;
}

const pause = async (ms) => {
  if (ms > 0) await new Promise((resolve) => setTimeout(resolve, ms));
};
const timeout = (message) => Object.assign(new Error(message), { reason: "timeout" });

async function runChatUser(target, config, run, behavior) {
  initializeLoadMetrics(config.tags);
  const client = new ChatClient(target.memberId, config);
  const tracker = new DeliveryTracker(target, run.runId, config.timeoutMs);
  const sequences = new Map(target.rooms.map((room) => [room.threadId, 0]));
  const acknowledged = new Map(sequences);
  const controlInterval = Math.min(500, Math.max(20, config.startupMs / 10));
  let failure = null, observerFailure = null;
  let connected = false, ready = false, closed = false, sent = 0, received = 0, stage = "connect";
  let actionResult;
  const report = (error, fallback) => {
    if (failure === error) return;
    if (!failure) failure = error;
    recordFailure(error.stage || fallback, error, config.tags);
  };
  client.onEvent((event, receivedAt) => {
    try {
      const result = tracker.observe(event, receivedAt);
      if (result) {
        recordDelivery(result, config.tags);
        if (!result.duplicate) received++;
        if (result.late) report(timeout("전달 허용 시간 초과"), "delivery");
      }
    } catch (error) {
      if (!observerFailure) { observerFailure = error; report(error, "delivery"); }
    }
  });
  const healthy = () => {
    if (client.failure) throw client.failure;
    if (observerFailure) throw observerFailure;
  };
  const send = async (room, kind, sequence = 0) => {
    const message = client.createMessage(room.threadId, { measured: kind === "message" });
    const remaining = (kind === "done" ? run.drainUntil : kind === "message"
      ? run.drainUntil : run.startAt) - Date.now();
    if (remaining <= 0) throw timeout("전송 단계의 대기 기한 초과");
    return client.sendMessage(message, { timeoutMs: Math.min(config.timeoutMs, remaining),
      contentAtSend: (sentAt) => loadContent(run.runId, kind, message, sentAt, sequence) });
  };
  const sendMessage = async (room) => {
    healthy();
    if (!target.rooms.some((joined) => joined.threadId === room?.threadId)) {
      throw new Error("사용자가 참여한 방에만 측정 메시지를 보낼 수 있습니다");
    }
    if (Date.now() >= run.endAt) throw timeout("측정 메시지 전송 기간이 끝났습니다");
    const sequence = sequences.get(room.threadId) + 1;
    sequences.set(room.threadId, sequence);
    try {
      const ack = await send(room, "message", sequence);
      acknowledged.set(room.threadId, sequence);
      sent++;
      recordAckResult(true, config.tags);
      return ack;
    } catch (error) {
      recordAckResult(false, config.tags);
      report(error, "ack");
      throw error;
    }
  };
  const waitUntilEnd = async () => {
    while (Date.now() < run.endAt) {
      healthy();
      await pause(Math.min(100, run.endAt - Date.now()));
    }
  };
  try {
    await client.open();
    connected = true;
    stage = "subscription";
    while (Date.now() < run.startAt && !tracker.armed) {
      healthy();
      for (const room of target.rooms) await send(room, "ready");
      if (tracker.ready) for (const room of target.rooms) await send(room, "armed");
      await pause(Math.min(controlInterval, run.startAt - Date.now()));
    }
    if (!tracker.armed) throw timeout("모든 방 참여자의 구독 준비 확인 실패");
    ready = true;
    await pause(run.startAt - Date.now());
    stage = "action";
    try {
      actionResult = await behavior.act({ target, client, config, run, sendMessage, pause, healthy, waitUntilEnd });
    } catch (error) { report(error, stage); }
    stage = "completion";
    // 마지막 ACK 순번을 전달해 수신자가 예상 건수와 누락을 계산한다. 준비·종료 신호는 측정에서 제외한다.
    let announced = false;
    while (Date.now() < run.drainUntil) {
      healthy();
      if (!announced || tracker.summary().completionMissing) {
        if (run.drainUntil - Date.now() < config.timeoutMs) {
          await pause(run.drainUntil - Date.now());
          break;
        }
        for (const room of target.rooms) await send(room, "done", acknowledged.get(room.threadId));
        announced = true;
      }
      const summary = tracker.summary();
      if (!summary.completionMissing && !summary.missing) break;
      await pause(Math.min(controlInterval, run.drainUntil - Date.now()));
    }
  } catch (error) {
    if (error !== observerFailure) report(error, stage);
  } finally {
    const summary = tracker.summary();
    recordDeliverySummary(summary, config.tags);
    if (summary.missing) report(timeout("ACK 완료 메시지의 수신 누락"), "delivery");
    if (summary.completionMissing) report(timeout("송신 완료 신호 수신 누락"), "completion");
    if (client.failure && !failure) report(client.failure, "connection");
    try { await client.close(); closed = true; } catch (error) { report(error, "cleanup"); }
    const state = { target, connected, ready, closed, sent, received, delivery: summary, failure, actionResult };
    check(state, {
      ...behavior.checks,
      "회원 STOMP 인증 완료": () => connected,
      "방 참여자 구독 준비 완료": () => ready,
      "예상 메시지와 송신 완료 신호 수신": () => !summary.missing && !summary.completionMissing,
      "연결 종료": () => closed,
      "반복 부하 오류 없음": () => failure === null,
    }, config.tags);
  }
  if (failure) throw failure;
}
