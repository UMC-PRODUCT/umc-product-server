import { check } from "k6";

import { ReactionClient } from "./reaction-client.js";
import { StompError } from "../stomp/stomp.js";
import { createReactionPlan } from "../../config/chat-reaction.js";
import { initializeReactionMetrics, reactionFailure, reactionReady, reactionIntegrity } from "./reaction-metrics.js";
import { runFixedSchedule } from "./fixed-schedule.js";
import { recordSchedule } from "./schedule-metrics.js";

const pause = (milliseconds) => milliseconds > 0
  ? new Promise((resolve) => setTimeout(resolve, milliseconds)) : Promise.resolve();

function requireCheck(ok, name, tags) {
  check(ok, { [name]: (value) => value }, tags);
  if (!ok) throw new StompError("리액션 검증 실패: " + name, "mismatch");
}

async function verifyCount(client, expected) {
  const state = await client.getReaction();
  requireCheck(state.count === expected && state.reactedByMe === (expected === 1),
    "리액션 개수와 본인 상태 일치", client.config.tags);
}

export async function prepareReactionRun(seed, config) {
  const plan = createReactionPlan(seed, config);
  const client = new ReactionClient(plan.targets[0], config, "reaction-prepare");
  const runId = crypto.randomUUID();
  try {
    await client.open();
    const room = await client.read("/api/v1/community/threads/" + plan.threadId);
    requireCheck(room.threadId === plan.threadId && room.isJoined === true
      && String(room.memberCount) === String(plan.memberIds.length), "실제 방 참여 인원 일치", config.tags);
    // 메시지 준비는 setup에서만 수행하고 재측정에서는 conditions.json의 messageIds를 재사용한다.
    for (let index = 0; index < plan.targets.length; index++) {
      const target = plan.targets[index];
      if (target.messageId) continue;
      const message = client.createMessage(plan.threadId, { measured: false,
        content: "k6 reaction " + runId + " " + index });
      const ack = await client.sendMessage(message);
      if (typeof ack.messageId !== "string" || !/^[1-9][0-9]*$/.test(ack.messageId)) {
        throw new StompError("준비 메시지 ACK의 메시지 ID가 올바르지 않습니다", "mismatch");
      }
      target.messageId = ack.messageId;
      // 한 준비용 사용자의 초당 SEND 제한을 넘기지 않는다.
      await pause(100);
    }
  } finally { await client.close(); }
  const warmupStartAt = Date.now() + config.startupMs;
  const warmupEndAt = warmupStartAt + config.warmupMs;
  // 워밍업의 마지막 요청, 초기 상태 복구와 조회를 마친 뒤 공통 시각에 측정한다.
  const startAt = warmupEndAt + config.timeoutMs * 3;
  return { runId, plan, warmupStartAt, warmupEndAt, startAt, endAt: startAt + config.durationMs,
    reactionSeed: { baseUrl: config.baseUrl, memberIds: plan.memberIds,
      rooms: [{ threadId: plan.threadId, memberIds: plan.memberIds,
        messageIds: plan.targets.map((target) => target.messageId) }] } };
}

async function repeatReactions(client, window) {
  const config = client.config;
  const measured = window.phase === "measure";
  const results = await runFixedSchedule({ startAt: window.startAt,
    measureAt: measured ? window.startAt : window.endAt, endAt: window.endAt,
    intervalMs: config.intervalMs, offsetMs: window.offsetMs,
    maxInflight: config.maxInflight }, {
    healthy() { if (client.failure) throw client.failure; },
    send: (slot) => client.changeReaction(slot % 2 === 0 ? "add" : "remove", window.phase),
    record: (sample) => recordSchedule(sample, { ...config.tags,
      phase: sample.phase, user_slot: window.userSlot }),
  }, { now: () => Date.now(), pause });
  return results.reduce((total, phase) => total + phase.issued, 0);
}

async function measureReactions(client, run) {
  const config = client.config;
  const userSlot = run.plan.targets.findIndex((target) => target.memberId === client.target.memberId);
  const offsetMs = Math.floor(userSlot * config.intervalMs / run.plan.targets.length);
  requireCheck(Date.now() < run.warmupStartAt, "공통 시작 전 구독과 준비 완료", config.tags);
  await pause(run.warmupStartAt - Date.now());
  await repeatReactions(client, { phase: "warmup", startAt: run.warmupStartAt,
    endAt: run.warmupEndAt, offsetMs, userSlot: userSlot + 1 });
  await client.changeReaction("remove");
  await verifyCount(client, 0);
  requireCheck(Date.now() < run.startAt, "공통 측정 시작 전 초기 상태 복구 완료", config.tags);
  reactionReady(config.tags);
  await pause(run.startAt - Date.now());
  const count = await repeatReactions(client, { phase: "measure", startAt: run.startAt,
    endAt: run.endAt, offsetMs, userSlot: userSlot + 1 });
  requireCheck(count > 0, "측정 리액션 ACK 한 건 이상", config.tags);
  const valid = client.integrity.matches(await client.getReaction());
  reactionIntegrity(valid, config.tags);
  requireCheck(valid, "정리 전 저장 상태와 실제 변경 ACK 일치", config.tags);
  await client.changeReaction("remove");
  await verifyCount(client, 0);
}

async function verifyDuplicate(clients, config) {
  const [first, second] = clients;
  reactionReady(config.tags);
  for (let round = 0; round < config.rounds; round++) {
    const startedAt = Date.now();
    await first.changeReaction("remove");
    await verifyCount(first, 0);
    // 같은 회원의 두 연결에서 다른 commandId로 전송한다. 요청 재전송 중복 제거를 시험하는 것이 아니다.
    const results = await Promise.allSettled(clients.map((client) => client.changeReaction("add", "measure")));
    const failed = results.find((result) => result.status === "rejected");
    if (failed) throw failed.reason;
    requireCheck(results.filter((result) => result.value.deduplicated === false).length === 1,
      "동시 추가 두 건 중 실제 추가는 한 건", config.tags);
    await verifyCount(second, 1);
    await pause(startedAt + config.intervalMs - Date.now());
  }
  // DB 직접 조회로도 확인할 수 있도록 마지막 리액션 한 건은 남긴다.
}

export async function runReactionUser(target, config, run) {
  initializeReactionMetrics(config.tags);
  const clients = [new ReactionClient(target, config, "reaction-user")];
  if (config.mode === "duplicate") clients.push(new ReactionClient(target, config, "reaction-peer"));
  let failure = null;
  try {
    const prepared = await Promise.allSettled(clients.map((client) => client.prepare()));
    const failed = prepared.find((result) => result.status === "rejected");
    if (failed) throw failed.reason;
    switch (config.mode) {
      case "load": await measureReactions(clients[0], run); break;
      case "duplicate": await verifyDuplicate(clients, config); break;
      default: throw new StompError("알 수 없는 리액션 모드", "mismatch");
    }
  } catch (error) {
    if (!(error instanceof Error)) throw error;
    failure = error;
    reactionFailure(config.tags, error);
  } finally {
    const closed = await Promise.allSettled(clients.map((client) => client.close()));
    const error = closed.find((result) => result.status === "rejected")?.reason;
    if (error) { reactionFailure(config.tags, error); failure ||= error; }
  }
  if (failure) throw failure;
}
