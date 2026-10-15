import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test, { beforeEach, afterEach } from "node:test";

import { encodeFrame, StompDecoder } from "../lib/stomp/stomp-protocol.js";

const state = globalThis.__chatSmokeHarness = {};
globalThis.__ENV = { BASE_URL: "http://localhost:8080", CHAT_TIMEOUT_MS: "100" };
const realNow = Date.now;

class FakeWebSocket {
  static OPEN = 1;
  static CLOSED = 3;

  constructor(_url, _protocols, options) {
    this.url = _url;
    this.role = options.tags.client;
    this.readyState = 0;
    this.decoder = new StompDecoder();
    state.sockets.push(this);
    this.schedule(() => { this.readyState = 1; this.onopen(); }, 0);
  }

  schedule(callback, delay) {
    const timer = setTimeout(() => { state.timers.delete(timer); callback(); }, state.fast ? 0 : delay);
    state.timers.add(timer);
  }

  emit(event, copies = 1) {
    if (this.readyState === 3) return;
    const frame = encodeFrame("MESSAGE", {}, JSON.stringify(event));
    this.onmessage({ data: frame.repeat(copies) });
  }

  send(data) {
    for (const frame of this.decoder.push(data)) {
      if (frame.command === "CONNECT") {
        this.memberId = /test-token-([0-9]+)/.exec(frame.headers.Authorization)?.[1];
        this.schedule(() => this.onmessage({ data: state.mode === "auth-error"
          ? encodeFrame("ERROR", {}, "인증 실패")
          : encodeFrame("CONNECTED", { version: "1.2", "heart-beat": "0,0" }) }), 0);
      }
      if (frame.command !== "SEND") continue;
      const payload = JSON.parse(frame.body);
      let wire;
      try { wire = JSON.parse(payload.content).__k6_chat; } catch (_) {}
      const main = wire ? wire.kind === "message" : !payload.content.includes("준비");
      state.sends.push({ main, payload, memberId: this.memberId });
      if (main && state.mode === "send-error") throw new Error("테스트 전송 실패");
      if (state.mode === "preflight-timeout") continue;
      const receivers = state.sockets.filter((socket) => socket !== this && socket.readyState === 1);
      const sentAt = state.now;
      const threadId = /threads\/([0-9]+)\//.exec(frame.headers.destination)[1];
      const members = state.rooms?.find((room) => room.threadId === threadId)?.memberIds;
      const message = { ...payload, threadId, senderId: this.memberId, messageId: main ? "20" : "10" };
      const ack = { type: "command.acknowledged", threadId, payload: {
        commandId: frame.headers["x-command-id"], command: "MESSAGE_CREATE",
        clientMessageId: payload.clientMessageId, messageId: message.messageId, deduplicated: false,
      } };
      const delivery = { type: "message.created", threadId,
        payload: { clientMessageId: payload.clientMessageId, message } };
      if (main && state.mode === "disconnect") {
        this.schedule(() => { this.readyState = 3; this.onclose(); }, 5);
        continue;
      }
      if (main && state.mode === "rejected" && (!state.rejectMemberId || this.memberId === state.rejectMemberId)) {
        this.schedule(() => this.emit({ commandId: frame.headers["x-command-id"],
          status: 403, code: "DENIED" }), 5);
        continue;
      }
      const ackDelay = main && state.mode === "delivery-first" ? 40 : 10;
      const deliveryDelay = main && state.mode === "delivery-first" ? 10 : 40;
      if (!(main && ["ack-timeout", "both-timeout"].includes(state.mode))) {
        this.schedule(() => {
          state.now = sentAt + ackDelay;
          if (main && state.mode === "ack-mismatch") ack.payload.command = "WRONG";
          this.emit(ack, main && state.mode === "duplicate" ? 2 : 1);
        }, ackDelay);
      }
      if (!(main && ["delivery-timeout", "both-timeout"].includes(state.mode))) {
        this.schedule(() => {
          if (wire?.kind === "done" && state.mode === "completion-timeout") return;
          state.now = sentAt + deliveryDelay;
          if (main && state.mode === "delivery-mismatch") message.content = "다른 본문";
          if (main && state.mode === "id-mismatch") message.messageId = "999";
          // 발신자에게 돌아온 이벤트는 상대방 연결의 중복 수신과 구분한다.
          this.emit(delivery);
          for (const receiver of receivers.filter((socket) => !members || members.includes(socket.memberId))) {
            receiver.emit(delivery, main && state.mode === "duplicate" ? 2 : 1);
            if (main) receiver.lastDelivery = delivery;
          }
        }, deliveryDelay);
      }
    }
  }

  close() {
    if (this.readyState === 3) return;
    this.readyState = 2;
    this.schedule(() => {
      if (state.mode === "duplicate" && this.lastDelivery) this.emit(this.lastDelivery);
      this.readyState = 3;
      this.onclose();
    }, 0);
  }
}

state.WebSocket = FakeWebSocket;
registerHooks({
  resolve(specifier, context, nextResolve) {
    if (specifier === "k6" || specifier.startsWith("k6/")) {
      return { url: `mock:${specifier}`, shortCircuit: true };
    }
    return nextResolve(specifier, context);
  },
  load(url, context, nextLoad) {
    const modules = {
      "mock:k6": `export function check(result, checks) {
        const state = globalThis.__chatSmokeHarness;
        state.checks = Object.values(checks).map(fn => fn(result));
        state.checkRecords.push({ result, names: Object.keys(checks), values: state.checks });
        return state.checks.every(Boolean);
      }`,
      "mock:k6/http": `export default { get(url) {
        globalThis.__chatSmokeHarness.authRequests.push(url);
        return { status: 200, body: "test-token-" + new URL(url).searchParams.get("memberId") };
      } };`,
      "mock:k6/websockets": "export const WebSocket = globalThis.__chatSmokeHarness.WebSocket;",
      "mock:k6/execution": "export const vu = { get idInTest() { return globalThis.__chatSmokeHarness.vuId; } };",
      "mock:k6/metrics": `class Metric {
        constructor(name) { this.name = name; }
        add(value, tags) {
          const samples = globalThis.__chatSmokeHarness.samples;
          (samples[this.name] ||= []).push({ value, tags });
        }
      }
      export { Metric as Counter, Metric as Rate, Metric as Trend };`,
    };
    if (url in modules) return { format: "module", source: modules[url], shortCircuit: true };
    return nextLoad(url, context);
  },
});

const { default: smoke } = await import("../scenarios/smoke/chat-message.js");
const { default: load, validateSeed: validateLoadSeed, prepareRun, getOptions: loadOptions } = await import("../scenarios/load/chat-message.js");
const { runChatUsers } = await import("../lib/chat/chat-load-session.js");
const { createLoadPlan } = await import("../lib/chat/chat-load.js");
const { readLoadConfig } = await import("../config/chat-load.js");
const { repeatMessages } = await import("../scenarios/helpers/chat-message-behavior.js");
const { runChatPair, selectPair } = await import("../scenarios/helpers/chat-pair.js");
const { readPairConfig } = await import("../config/chat-pair.js");
const { ChatClient } = await import("../lib/chat/chat-client.js");
const { readChatConfig } = await import("../config/chat.js");
const { StompError } = await import("../lib/stomp/stomp.js");
const seed = { baseUrl: "http://localhost:8080", memberIds: ["2", "3"],
  rooms: [{ threadId: "1", memberIds: ["2", "3"] }] };
const samples = (name) => state.samples[`umc_chat_${name}`] || [];
const count = (name) => samples(name).reduce((sum, sample) => sum + Number(sample.value), 0);
const issues = () => samples("failures").filter((sample) => sample.value);

beforeEach(() => {
  Object.assign(state, { mode: "normal", sockets: [], sends: [], samples: {},
    checks: [], checkRecords: [], authRequests: [], timers: new Set(), now: 1000, fast: false, vuId: 1, rooms: null, rejectMemberId: null });
  globalThis.__ENV = { BASE_URL: "http://localhost:8080", CHAT_TIMEOUT_MS: "100" };
  // 실제 타이머로 응답 순서를 바꾸되 논리 시각은 고정해 지연 계산을 정확한 숫자로 검증한다.
  Date.now = () => state.now;
});

afterEach(() => {
  Date.now = realNow;
  for (const timer of state.timers) clearTimeout(timer);
  assert.ok(state.sockets.every((socket) => socket.readyState === 3));
});

test("준비 메시지를 제외하고 ACK 10ms와 전달 40ms를 각각 한 번 기록한다", async () => {
  await smoke(seed);
  assert.equal(state.sends.length, 2);
  assert.equal(count("message_attempts"), 1);
  assert.equal(count("message_successes"), 1);
  assert.equal(count("message_failures"), 0);
  assert.deepEqual(samples("message_error_rate").map((entry) => entry.value), [false]);
  assert.deepEqual(samples("ack_latency_ms").map((entry) => entry.value), [10]);
  assert.deepEqual(samples("delivery_latency_ms").map((entry) => entry.value), [40]);
  assert.ok(state.checks.every(Boolean));
});

test("상대방 수신이 먼저 와도 각 도착 시각으로 지연을 기록한다", async () => {
  state.mode = "delivery-first";
  await smoke(seed);
  assert.equal(samples("ack_latency_ms")[0].value, 40);
  assert.equal(samples("delivery_latency_ms")[0].value, 10);
});

test("중복 ACK·전달과 종료 중 늦은 중복은 지연·성공을 다시 집계하지 않는다", async () => {
  state.mode = "duplicate";
  await smoke(seed);
  assert.equal(count("duplicate_events"), 3);
  assert.equal(count("message_successes"), 1);
  assert.equal(samples("ack_latency_ms").length, 1);
  assert.equal(samples("delivery_latency_ms").length, 1);
});

for (const [mode, stage, reason, ackSamples, deliverySamples] of [
  ["rejected", "ack", "rejected", 0, 0],
  ["ack-timeout", "ack", "timeout", 0, 1],
  ["delivery-timeout", "delivery", "timeout", 1, 0],
  ["both-timeout", "delivery", "timeout", 0, 0],
  ["send-error", "send", "send_error", 0, 0],
  ["ack-mismatch", "ack", "mismatch", 0, 0],
  ["delivery-mismatch", "delivery", "mismatch", 1, 0],
  ["id-mismatch", "correlation", "mismatch", 1, 1],
  ["disconnect", "ack", "connection_closed", 0, 0],
]) {
  test(`${mode}: 실패 한 건과 실제 원인만 기록하고 시간 초과를 지연에 넣지 않는다`, async () => {
    state.mode = mode;
    await assert.rejects(smoke(seed));
    assert.equal(count("message_attempts"), 1);
    assert.equal(count("message_failures"), 1);
    assert.equal(count("message_successes"), 0);
    assert.deepEqual(samples("message_error_rate").map((entry) => entry.value), [true]);
    assert.deepEqual(issues().map((entry) => [entry.tags.stage, entry.tags.reason]), [[stage, reason]]);
    assert.equal(samples("ack_latency_ms").length, ackSamples);
    assert.equal(samples("delivery_latency_ms").length, deliverySamples);
    assert.equal(state.checks[5], true);
    assert.equal(state.checks[6], false);
  });
}

for (const [mode, stage] of [["auth-error", "connect"], ["preflight-timeout", "subscription"]]) {
  test(`${mode}: 준비 실패는 메시지 실패율의 분모에 포함하지 않는다`, async () => {
    state.mode = mode;
    await assert.rejects(smoke(seed));
    assert.equal(count("message_attempts"), 0);
    assert.equal(count("message_failures"), 0);
    assert.equal(samples("message_error_rate").length, 0);
    assert.equal(samples("ack_latency_ms").length, 0);
    assert.equal(samples("delivery_latency_ms").length, 0);
    assert.equal(issues().length, 1);
    assert.equal(issues()[0].tags.stage, stage);
    if (mode === "preflight-timeout") assert.equal(state.sends.length, 3);
  });
}

test("600건을 보내도 연결·관측자·수신 버퍼·중복 기록이 누적되지 않는다", async () => {
  state.fast = true;
  const config = readPairConfig(__ENV);
  const { session, failure } = await runChatPair(selectPair(seed, config), config, async (chat) => {
    for (let index = 0; index < 600; index++) await chat.sendMessage();
    for (const client of chat.clients) {
      assert.equal(client.history.size, 512);
      assert.equal(client.stomp.observers.size, 1);
      assert.equal(client.stomp.inbox.length, 0);
      assert.equal(client.stomp.waiters.length, 0);
    }
  });
  assert.equal(failure, null);
  assert.equal(state.sockets.length, 2);
  assert.equal(count("message_successes"), 600);
  assert.equal(samples("ack_latency_ms").length, 600);
  assert.ok(session.clients.every((client) => client.history.size === 0));
  assert.ok(session.clients.every((client) => client.stomp.observers.size === 0));
});

test("한 요청의 실패가 별개 키의 응답 대기까지 취소하지 않는다", async () => {
  const config = readPairConfig(__ENV);
  const { failure } = await runChatPair(selectPair(seed, config), config, async (chat) => {
    const receiver = chat.clients[1].stomp;
    const independent = receiver.waitFor((frame) => frame.command === "RECEIPT", "별개 요청", 100, "other");
    state.mode = "rejected";
    await assert.rejects(chat.sendMessage());
    assert.equal(receiver.waiters.length, 1);
    receiver.dispatch({ command: "RECEIPT", headers: {}, body: "" });
    assert.equal((await independent).command, "RECEIPT");
  });
  assert.equal(failure.reason, "rejected");
});

test("Smoke는 첫 번째 방의 두 회원만 선택하고 추가 방은 검사하지 않는다", () => {
  const config = readPairConfig(__ENV);
  const data = { ...seed, rooms: [seed.rooms[0], seed.rooms[0], null] };
  assert.deepEqual(selectPair(data, config), { threadId: "1", senderId: "2", receiverId: "3" });
  assert.equal(state.sockets.length, 0);
});

test("Smoke의 첫 번째 방이 없거나 두 회원 구성이 잘못되면 연결 전에 거부한다", () => {
  const config = readPairConfig(__ENV);
  for (const rooms of [undefined, null, [], {}]) {
    assert.throws(() => selectPair({ ...seed, rooms }, config), /부족/);
  }
  for (const room of [null, { threadId: "0", memberIds: ["2", "3"] },
    { threadId: "1", memberIds: ["2"] }, { threadId: "1", memberIds: ["2", "3", "4"] },
    { threadId: "1", memberIds: ["2", "99"] }, { threadId: "1", memberIds: [2, 3] }]) {
    assert.throws(() => selectPair({ ...seed, rooms: [room, seed.rooms[0]] }, config), /유효한 회원/);
  }
  assert.throws(() => selectPair({ ...seed, rooms: [{ threadId: "1", memberIds: ["2", "2"] }] }, config), /서로 다른/);
  assert.equal(state.sockets.length, 0);
});

test("반복 부하의 사용자 수와 시드가 다르면 연결 전에 거부한다", () => {
  __ENV.CHAT_USERS = "4";
  assert.throws(() => validateLoadSeed(seed), /일치/);
  assert.equal(state.sockets.length, 0);
});

async function runUsers(data, { usersPerVu, vuUsers, senders } = {}) {
  Date.now = realNow;
  __ENV.CHAT_USERS = String(data.memberIds.length);
  __ENV.CHAT_ROOMS = String(data.rooms.length);
  __ENV.CHAT_STARTUP_TIMEOUT_MS = "400";
  __ENV.DURATION = "180ms";
  __ENV.CHAT_MESSAGE_INTERVAL_MS = "60";
  if (usersPerVu !== undefined) __ENV.CHAT_USERS_PER_VU = String(usersPerVu);
  if (vuUsers !== undefined) __ENV.CHAT_VU_USERS = JSON.stringify(vuUsers);
  if (senders !== undefined) __ENV.CHAT_SENDERS = JSON.stringify(senders);
  state.rooms = data.rooms;
  const context = prepareRun();
  const results = await Promise.allSettled(Array.from({ length: loadOptions().scenarios.chat_load.vus }, (_, index) => {
    state.vuId = index + 1;
    return load(data, JSON.parse(JSON.stringify(context)));
  }));
  return results;
}

test("독립된 네 VU가 같은 방에서 전송하고 각각 세 명에게 전달한다", async () => {
  const data = { ...seed, memberIds: ["2", "3", "4", "5"],
    rooms: [{ threadId: "1", memberIds: ["2", "3", "4", "5"] }] };
  const results = await runUsers(data);
  assert.deepEqual(results.map((entry) => entry.status), Array(4).fill("fulfilled"));
  assert.equal(state.sockets.length, 4);
  assert.equal(count("message_attempts"), count("ack_successes"));
  assert.ok(count("ack_successes") >= 8);
  assert.equal(count("delivery_expected"), count("ack_successes") * 3);
  assert.equal(count("delivery_received"), count("delivery_expected"));
  assert.equal(count("delivery_missing"), 0);
  assert.equal(count("completion_missing"), 0);
  assert.equal(samples("delivery_latency_ms").length, count("delivery_received"));
  assert.ok(samples("delivery_latency_ms").every((sample) => sample.value >= 0 && sample.value <= 100));
});

test("사용자 수가 같아도 방을 둘로 나누면 각 메시지의 수신자는 한 명이다", async () => {
  const data = { ...seed, memberIds: ["2", "3", "4", "5"], rooms: [
    { threadId: "1", memberIds: ["2", "3"] }, { threadId: "2", memberIds: ["4", "5"] }] };
  const results = await runUsers(data);
  assert.ok(results.every((entry) => entry.status === "fulfilled"));
  assert.equal(state.sockets.length, 4);
  assert.equal(count("delivery_expected"), count("ack_successes"));
  assert.equal(count("delivery_received"), count("ack_successes"));
});

test("한 VU의 여러 사용자가 각각 연결과 전송 주기를 유지한다", async () => {
  const data = { ...seed, memberIds: ["2", "3", "4", "5"],
    rooms: [{ threadId: "1", memberIds: ["2", "3", "4", "5"] }] };
  const results = await runUsers(data, { usersPerVu: 2 });
  assert.deepEqual(results.map((result) => result.status), ["fulfilled", "fulfilled"]);
  assert.equal(state.sockets.length, 4);
  assert.ok(count("ack_successes") >= 8);
  assert.equal(count("delivery_expected"), count("ack_successes") * 3);
  assert.equal(count("delivery_received"), count("delivery_expected"));
  assert.equal(count("delivery_missing"), 0);
});

test("서로 다른 인원을 맡은 VU들이 방 배치와 무관하게 송수신한다", async () => {
  const data = { ...seed, memberIds: ["2", "3", "4", "5", "6"], rooms: [
    { threadId: "1", memberIds: ["2", "3", "4"] }, { threadId: "2", memberIds: ["4", "5", "6"] }] };
  const results = await runUsers(data, { vuUsers: [[1, 4], [2, 3, 5]] });
  assert.deepEqual(results.map((result) => result.status), ["fulfilled", "fulfilled"]);
  assert.equal(state.sockets.length, 5);
  assert.ok(count("ack_successes") >= 10);
  assert.equal(count("delivery_expected"), count("ack_successes") * 2);
  assert.equal(count("delivery_received"), count("delivery_expected"));
});

test("모든 사용자를 한 VU에 배정해도 같은 측정 코드로 전달을 확인한다", async () => {
  const results = await runUsers(seed, { usersPerVu: 2 });
  assert.deepEqual(results.map((result) => result.status), ["fulfilled"]);
  assert.equal(state.sockets.length, 2);
  assert.equal(count("delivery_expected"), count("ack_successes"));
  assert.equal(count("delivery_received"), count("delivery_expected"));
});

test("공통 실행 함수는 VU 배정 설정 없이 시나리오가 넘긴 사용자 목록을 실행한다", async () => {
  Date.now = realNow;
  __ENV.CHAT_STARTUP_TIMEOUT_MS = "400";
  __ENV.DURATION = "120ms";
  __ENV.CHAT_MESSAGE_INTERVAL_MS = "60";
  const config = readLoadConfig(__ENV);
  delete config.vuUsers;
  const targets = createLoadPlan(seed, config).reverse();
  await runChatUsers(targets, config, prepareRun(), () => repeatMessages(config.intervalMs));
  assert.equal(state.sockets.length, 2);
  assert.equal(count("delivery_missing"), 0);
  await assert.rejects(runChatUsers([], config, prepareRun()), /목록/);
  await assert.rejects(runChatUsers([targets[0], targets[0]], config, prepareRun()), /목록/);
});

test("한 명만 송신하고 나머지는 수신하며 사용자별 성공 조건을 적용한다", async () => {
  const data = { ...seed, memberIds: ["2", "3", "4", "5"],
    rooms: [{ threadId: "1", memberIds: ["2", "3", "4", "5"] }] };
  const results = await runUsers(data, { senders: [1], vuUsers: [[1, 3], [2, 4]] });
  assert.ok(results.every((result) => result.status === "fulfilled"));
  assert.deepEqual([...new Set(state.sends.filter((send) => send.main).map((send) => send.memberId))], ["2"]);
  assert.equal(count("delivery_expected"), count("ack_successes") * 3);
  assert.equal(count("delivery_received"), count("delivery_expected"));
  const records = state.checkRecords.filter((record) => record.result?.target);
  assert.equal(records.length, 4);
  for (const record of records) {
    assert.ok(record.values.every(Boolean));
    if (record.result.target.memberId === "2") assert.ok(record.result.sent > 0);
    else {
      assert.equal(record.result.sent, 0);
      assert.ok(record.result.received > 0);
      assert.ok(record.names.includes("측정 메시지 수신 한 건 이상"));
      assert.ok(!record.names.includes("측정 메시지 ACK 한 건 이상"));
    }
  }
});

test("수신 사용자에게 메시지가 누락되면 송신자의 ACK 성공과 구분해 실패한다", async () => {
  state.mode = "delivery-timeout";
  const results = await runUsers(seed, { senders: [1] });
  assert.equal(results[0].status, "fulfilled");
  assert.equal(results[1].status, "rejected");
  assert.ok(count("ack_successes") > 0);
  assert.equal(count("delivery_missing"), count("ack_successes"));
  assert.equal(samples("delivery_latency_ms").length, 0);
});

test("시나리오는 반복 전송 대신 한 번 전송하는 행동과 반환값 검증을 직접 정의할 수 있다", async () => {
  Date.now = realNow;
  __ENV.CHAT_STARTUP_TIMEOUT_MS = "400";
  __ENV.DURATION = "180ms";
  const config = readLoadConfig(__ENV);
  await runChatUsers(createLoadPlan(seed, config), config, prepareRun(), () => ({
    async act({ target, sendMessage }) {
      const ack = await sendMessage(target.rooms[0]);
      return { messageId: ack.messageId };
    },
    checks: {
      "한 번만 전송": (state) => state.sent === 1,
      "행동 반환값 확인": (state) => Boolean(state.actionResult.messageId),
    },
  }));
  assert.equal(count("ack_successes"), 2);
  assert.ok(state.checkRecords.every((record) => record.values.every(Boolean)));
});

test("측정 메시지를 보내지 않는 행동에 공통 실행부가 ACK 성공 조건을 강제하지 않는다", async () => {
  Date.now = realNow;
  __ENV.CHAT_STARTUP_TIMEOUT_MS = "400";
  __ENV.DURATION = "120ms";
  const config = readLoadConfig(__ENV);
  await runChatUsers(createLoadPlan(seed, config), config, prepareRun(), () => ({
    async act({ waitUntilEnd }) { await waitUntilEnd(); },
    checks: { "측정 메시지 전송 없음": (state) => state.sent === 0 && state.received === 0 },
  }));
  assert.equal(count("message_attempts"), 0);
  assert.equal(count("delivery_missing"), 0);
  assert.ok(state.checkRecords.every((record) => record.values.every(Boolean)));
});

test("잘못된 사용자 행동은 어떤 연결도 만들기 전에 거부한다", async () => {
  const config = readLoadConfig(__ENV);
  const targets = createLoadPlan(seed, config);
  await assert.rejects(runChatUsers(targets, config, prepareRun()), /행동/);
  await assert.rejects(runChatUsers(targets, config, prepareRun(), (target) => target.memberId === "2"
    ? repeatMessages(config.intervalMs) : { act: null }), /act/);
  await assert.rejects(runChatUsers(targets, config, prepareRun(), () => ({
    act() {}, checks: { "잘못된 조건": true },
  })), /checks/);
  assert.equal(state.sockets.length, 0);
});

test("ChatClient의 인증 요청과 WebSocket 연결이 같은 서버 설정을 사용한다", async () => {
  const clients = ["http://server-a:8080", "http://server-b:8080"].map((baseUrl) =>
    new ChatClient("808", { ...readChatConfig(__ENV), baseUrl }));
  try {
    await Promise.all(clients.map((client) => client.open()));
    assert.deepEqual(state.authRequests, ["http://server-a:8080/test/token/access?memberId=808",
      "http://server-b:8080/test/token/access?memberId=808"]);
    assert.deepEqual(state.sockets.map((socket) => socket.url),
      ["ws://server-a:8080/ws/websocket", "ws://server-b:8080/ws/websocket"]);
  } finally { await Promise.all(clients.map((client) => client.close())); }
});

test("ACK만 성공하고 별도 VU의 수신이 누락되면 실패하며 지연 표본을 만들지 않는다", async () => {
  state.mode = "delivery-timeout";
  const results = await runUsers(seed);
  assert.ok(results.every((entry) => entry.status === "rejected"));
  assert.ok(count("ack_successes") > 0);
  assert.equal(count("delivery_missing"), count("ack_successes"));
  assert.equal(count("delivery_received"), 0);
  assert.equal(samples("delivery_latency_ms").length, 0);
  assert.equal(count("completion_missing"), 0);
});

test("중복 수신은 별도 집계하고 별도 VU의 전달 지연·건수를 늘리지 않는다", async () => {
  state.mode = "duplicate";
  const results = await runUsers(seed);
  assert.ok(results.every((entry) => entry.status === "fulfilled"));
  assert.ok(count("duplicate_events") > 0);
  assert.equal(count("delivery_received"), count("delivery_expected"));
  assert.equal(samples("delivery_latency_ms").length, count("delivery_expected"));
});

test("송신 완료 신호가 누락되면 데이터 수신이 성공해도 전체 성공으로 처리하지 않는다", async () => {
  state.mode = "completion-timeout";
  const results = await runUsers(seed);
  assert.ok(results.every((entry) => entry.status === "rejected"));
  assert.ok(count("delivery_received") > 0);
  assert.ok(count("completion_missing") > 0);
  assert.ok(issues().some((entry) => entry.tags.stage === "completion"));
});

test("ACK 실패는 전달 누락과 구분하고 모든 연결을 닫는다", async () => {
  state.mode = "rejected";
  const results = await runUsers(seed);
  assert.ok(results.every((entry) => entry.status === "rejected"));
  assert.equal(count("ack_failures"), 2);
  assert.equal(count("ack_successes"), 0);
  assert.equal(count("delivery_expected"), 0);
  assert.equal(count("delivery_missing"), 0);
  assert.ok(state.sockets.every((socket) => socket.readyState === 3));
});

test("한 VU에 배정된 사용자들이 실패해도 모두 종료된 뒤 실행 함수가 실패한다", async () => {
  state.mode = "rejected";
  const results = await runUsers(seed, { usersPerVu: 2 });
  assert.equal(results.length, 1);
  assert.equal(results[0].status, "rejected");
  assert.equal(count("ack_failures"), 2);
  assert.ok(state.sockets.every((socket) => socket.readyState === 3));
});

test("같은 VU의 한 사용자만 실패해도 다른 사용자는 송수신을 마치고 연결을 정리한다", async () => {
  state.mode = "rejected";
  state.rejectMemberId = "2";
  const results = await runUsers(seed, { usersPerVu: 2 });
  assert.equal(results[0].status, "rejected");
  assert.equal(count("ack_failures"), 1);
  assert.ok(count("ack_successes") >= 2);
  assert.equal(count("delivery_received"), count("ack_successes"));
  assert.equal(count("completion_missing"), 0);
  assert.ok(state.sockets.every((socket) => socket.readyState === 3));
});

test("여러 방에 참여하는 사용자는 연결을 추가하지 않고 참여 방을 순환한다", async () => {
  const data = { ...seed, memberIds: ["2", "3", "4"], rooms: [
    { threadId: "1", memberIds: ["2", "3"] }, { threadId: "2", memberIds: ["3", "4"] }] };
  const results = await runUsers(data);
  assert.ok(results.every((entry) => entry.status === "fulfilled"));
  assert.equal(state.sockets.length, 3);
  assert.equal(count("delivery_expected"), count("ack_successes"));
  assert.equal(count("delivery_missing"), 0);
});

test("배정되지 않은 VU는 연결 전에 실패한다", async () => {
  state.vuId = 3;
  await assert.rejects(load(seed, prepareRun()), /배정/);
  assert.equal(state.sockets.length, 0);
});

test("개별 사용자는 방 배정이나 상대방 없이 연결하고 자신의 ACK를 확인한다", async () => {
  const config = { ...readChatConfig(__ENV), tags: { scenario: "ack-only" } };
  const client = new ChatClient("2", config);
  try {
    await client.open();
    const message = client.createMessage("1");
    const ack = await client.sendMessage(message);
    assert.equal(ack.clientMessageId, message.clientMessageId);
    assert.equal(state.sockets.length, 1);
    assert.equal(samples("ack_latency_ms").length, 1);
    assert.equal(samples("delivery_latency_ms").length, 0);
    assert.equal(samples("message_error_rate").length, 0);
    assert.equal(samples("ack_latency_ms")[0].tags.scenario, "ack-only");
  } finally {
    await client.close();
  }
});

test("개별 수신자는 짝 구성 없이 이벤트 콜백을 등록하고 해제할 수 있다", async () => {
  const client = new ChatClient("3", readChatConfig(__ENV));
  let received = 0;
  try {
    await client.open();
    const remove = client.onEvent(() => received++);
    state.sockets[0].emit({ type: "message.created", threadId: "99", payload: {} });
    assert.equal(received, 1);
    remove();
    state.sockets[0].emit({ type: "message.created", threadId: "99", payload: {} });
    assert.equal(received, 1);
    assert.equal(client.stomp.inbox.length, 0);
  } finally {
    await client.close();
    assert.equal(client.listeners.size, 0);
  }
});

test("세 사용자가 같은 방에서 한 메시지를 보내고 두 수신자의 전달을 각각 확인한다", async () => {
  const config = { ...readChatConfig(__ENV), tags: { scenario: "three-members" } };
  const clients = ["2", "3", "4"].map((id) => new ChatClient(id, config));
  try {
    await Promise.all(clients.map((client) => client.open()));
    const message = clients[0].createMessage("1");
    const deliveries = clients.slice(1).map((client) => client.waitForMessage(message));
    const results = await Promise.all([clients[0].sendMessage(message), ...deliveries]);
    assert.ok(results.every((result) => result.messageId === results[0].messageId));
    assert.equal(state.sockets.length, 3);
    assert.equal(count("message_attempts"), 1);
    assert.equal(samples("ack_latency_ms").length, 1);
    assert.equal(samples("delivery_latency_ms").length, 2);
    assert.ok(samples("delivery_latency_ms").every((sample) => sample.tags.scenario === "three-members"));
  } finally {
    await Promise.all(clients.map((client) => client.close()));
  }
});

test("한 연결의 오류가 여러 대기를 깨워도 ACK와 수신의 실패 단계를 각각 유지한다", async () => {
  const client = new ChatClient("2", readChatConfig(__ENV));
  try {
    await client.open();
    await assert.rejects(client.open(), /한 번만/);
    const message = client.createMessage("1");
    const other = { ...message, clientMessageId: crypto.randomUUID(), commandId: crypto.randomUUID() };
    const pending = Promise.allSettled([client.sendMessage(message), client.waitForMessage(other)]);
    client.stomp.fail(new StompError("연결 종료 재현", "connection_closed"));
    const results = await pending;
    assert.deepEqual(results.map((entry) => entry.reason.stage), ["ack", "delivery"]);
    assert.ok(results.every((entry) => entry.reason.reason === "connection_closed"));
    assert.equal(state.sockets.length, 1);
  } finally {
    await client.close();
  }
});
