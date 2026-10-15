import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test, { beforeEach, afterEach } from "node:test";

import { encodeFrame, StompDecoder } from "../lib/stomp-protocol.js";

const state = globalThis.__chatSmokeHarness = {};
globalThis.__ENV = { BASE_URL: "http://localhost:8080", CHAT_TIMEOUT_MS: "100" };
const realNow = Date.now;

class FakeWebSocket {
  static OPEN = 1;
  static CLOSED = 3;

  constructor(_url, _protocols, options) {
    this.role = options.tags.client;
    this.readyState = 0;
    this.decoder = new StompDecoder();
    state.sockets.push(this);
    this.schedule(() => { this.readyState = 1; this.onopen(); }, 0);
  }

  schedule(callback, delay) {
    const timer = setTimeout(() => { state.timers.delete(timer); callback(); }, delay);
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
        this.schedule(() => this.onmessage({ data: state.mode === "auth-error"
          ? encodeFrame("ERROR", {}, "인증 실패")
          : encodeFrame("CONNECTED", { version: "1.2", "heart-beat": "0,0" }) }), 0);
      }
      if (frame.command !== "SEND") continue;
      const payload = JSON.parse(frame.body);
      const main = payload.content.includes("Smoke");
      state.sends.push({ main, payload });
      if (main && state.mode === "send-error") throw new Error("테스트 전송 실패");
      if (state.mode === "preflight-timeout") continue;
      const receiver = state.sockets.find((socket) => socket.role === "receiver");
      const sentAt = state.now;
      const message = { ...payload, threadId: "1", senderId: "2", messageId: main ? "20" : "10" };
      const ack = { type: "command.acknowledged", threadId: "1", payload: {
        commandId: frame.headers["x-command-id"], command: "MESSAGE_CREATE",
        clientMessageId: payload.clientMessageId, messageId: message.messageId, deduplicated: false,
      } };
      const delivery = { type: "message.created", threadId: "1",
        payload: { clientMessageId: payload.clientMessageId, message } };
      if (main && state.mode === "disconnect") {
        this.schedule(() => { this.readyState = 3; this.onclose(); }, 5);
        continue;
      }
      if (main && state.mode === "rejected") {
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
          state.now = sentAt + deliveryDelay;
          if (main && state.mode === "delivery-mismatch") message.content = "다른 본문";
          if (main && state.mode === "id-mismatch") message.messageId = "999";
          // 발신자에게 돌아온 이벤트는 상대방 연결의 중복 수신과 구분한다.
          this.emit(delivery);
          receiver.emit(delivery, main && state.mode === "duplicate" ? 2 : 1);
          if (main) receiver.lastDelivery = delivery;
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
        globalThis.__chatSmokeHarness.checks = Object.values(checks).map(fn => fn(result));
      }`,
      "mock:k6/http": 'export default { get() { return { status: 200, body: "test-token" }; } };',
      "mock:k6/websockets": "export const WebSocket = globalThis.__chatSmokeHarness.WebSocket;",
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
const seed = { baseUrl: "http://localhost:8080", memberIds: ["2", "3"],
  rooms: [{ threadId: "1", memberIds: ["2", "3"] }] };
const samples = (name) => state.samples[`umc_chat_${name}`] || [];
const count = (name) => samples(name).reduce((sum, sample) => sum + Number(sample.value), 0);
const issues = () => samples("failures").filter((sample) => sample.value);

beforeEach(() => {
  Object.assign(state, { mode: "normal", sockets: [], sends: [], samples: {},
    checks: [], timers: new Set(), now: 1000 });
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
  ["both-timeout", "ack", "timeout", 0, 0],
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
