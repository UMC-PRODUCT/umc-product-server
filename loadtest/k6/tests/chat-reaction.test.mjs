import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test, { beforeEach, afterEach } from "node:test";
import { StompDecoder, encodeFrame } from "../lib/stomp/stomp-protocol.js";
import { readReactionConfig } from "../config/chat-reaction.js";

const state = globalThis.__reactionHarness = {};
class FakeWebSocket {
  static OPEN = 1;
  static CLOSED = 3;
  constructor() {
    this.readyState = 1; this.decoder = new StompDecoder(); state.sockets.push(this);
    queueMicrotask(() => this.onopen());
  }
  emit(event) { this.onmessage({ data: encodeFrame("MESSAGE", {}, JSON.stringify(event)) }); }
  send(data) {
    for (const frame of this.decoder.push(data)) {
      if (frame.command === "CONNECT") {
        this.member = frame.headers.Authorization.replace("Bearer token-", "");
        queueMicrotask(() => this.onmessage({ data: encodeFrame("CONNECTED", { "heart-beat": "0,0" }) }));
      }
      if (frame.command !== "SEND") continue;
      const payload = JSON.parse(frame.body);
      const commandId = frame.headers["x-command-id"];
      const operation = /reactions\/(add|remove)$/.exec(frame.headers.destination)?.[1];
      let ack;
      if (operation) {
        const messageId = /messages\/([0-9]+)\//.exec(frame.headers.destination)[1];
        const existing = state.rows.some((row) => row.messageId === messageId && row.member === this.member);
        if (operation === "add" && (!existing || state.brokenUnique)) state.rows.push({ messageId, member: this.member });
        if (operation === "remove") state.rows = state.rows.filter((row) => row.messageId !== messageId || row.member !== this.member);
        state.commands.push({ operation, commandId, messageId });
        ack = { commandId, command: state.mismatch ? "WRONG" : "REACTION_" + operation.toUpperCase(),
          messageId, deduplicated: operation === "add" ? existing : !existing };
      } else {
        const messageId = String(1000 + state.messages.length);
        state.messages.push({ messageId, deletedAt: null });
        ack = { commandId, command: "MESSAGE_CREATE", messageId, clientMessageId: payload.clientMessageId, deduplicated: false };
      }
      queueMicrotask(() => {
        if (operation === "add" && state.silentAdd) return;
        const event = operation === "add" && state.reject ? { commandId, status: 403 }
          : { type: "command.acknowledged", threadId: "10", payload: ack };
        for (const socket of state.sockets.filter((socket) => socket.member === this.member && socket.readyState === 1)) socket.emit(event);
      });
    }
  }
  close() { this.readyState = 3; queueMicrotask(() => this.onclose()); }
}
state.WebSocket = FakeWebSocket;
registerHooks({
  resolve(specifier, context, next) {
    return specifier === "k6" || specifier.startsWith("k6/")
      ? { url: "reaction-mock:" + specifier, shortCircuit: true } : next(specifier, context);
  },
  load(url, context, next) {
    const modules = {
      "reaction-mock:k6": `export const check = (value, checks) => Object.values(checks).every(fn => fn(value));`,
      "reaction-mock:k6/metrics": `class Metric {
        constructor(name) { this.name = name; }
        add(value, tags) { (globalThis.__reactionHarness.samples[this.name] ||= []).push({ value, tags }); }
      } export { Metric as Counter, Metric as Trend, Metric as Rate };`,
      "reaction-mock:k6/websockets": "export const WebSocket = globalThis.__reactionHarness.WebSocket;",
      "reaction-mock:k6/http": `export default {
        get(url) { return { status: 200, body: 'token-' + new URL(url).searchParams.get('memberId') }; },
        async asyncRequest(method, url, body, params) {
          const state = globalThis.__reactionHarness;
          state.reads.push(url);
          const member = params.headers.Authorization.replace('Bearer token-', '');
          const result = url.includes('/messages?') ? { messages: state.messages.map(message => {
            const rows = state.rows.filter(row => row.messageId === message.messageId);
            return { ...message, reactions: rows.length ? [{ emoji: '👍', count: String(rows.length),
              reactedByMe: rows.some(row => row.member === member) }] : [] };
          }), hasMore: false } : { threadId: '10', memberCount: '1', isJoined: true };
          return { status: 200, json: () => ({ success: true, result }) };
        }
      };`,
    };
    return url in modules ? { format: "module", source: modules[url], shortCircuit: true } : next(url, context);
  },
});
globalThis.__ENV = {};
const { prepareReactionRun, runReactionUser } = await import("../lib/chat/reaction-run.js");
const { reactionSummary } = await import("../lib/chat/reaction-metrics.js");
const count = (name) => (state.samples["umc_chat_reaction_" + name] || []).reduce((sum, item) => sum + Number(item.value), 0);
const config = (extra = {}) => readReactionConfig({ CHAT_USERS: "1", CHAT_TIMEOUT_MS: "100",
  CHAT_STARTUP_TIMEOUT_MS: "500", CHAT_REACTION_WARMUP: "0s", DURATION: "200ms", ...extra });
const target = { threadId: "10", memberId: "1", messageId: "101" };
const seed = { baseUrl: "http://localhost:8080", memberIds: ["1"], rooms: [{ threadId: "10", memberIds: ["1"] }] };
beforeEach(() => Object.assign(state, { sockets: [], samples: {}, commands: [], reads: [], rows: [],
  messages: [{ messageId: "101", deletedAt: null }], brokenUnique: false, mismatch: false, reject: false, silentAdd: false }));
afterEach(() => assert.ok(state.sockets.every((socket) => socket.readyState === 3)));

test("중복 추가는 두 연결의 다른 요청을 구분하고 한 행과 한 번의 실제 추가를 확인한다", async () => {
  // Given
  const settings = config({ CHAT_REACTION_MODE: "duplicate", CHAT_REACTION_ROUNDS: "1" });
  // When
  await runReactionUser(target, settings, {});
  // Then
  assert.equal(state.sockets.length, 2);
  assert.equal(state.rows.length, 1);
  assert.equal(count("attempts"), 2);
  assert.equal(count("successes"), 2);
  assert.equal(new Set(state.commands.filter((item) => item.operation === "add").map((item) => item.commandId)).size, 2);
});

test("ACK가 성공해도 실제 조회에 중복 행이 보이면 검증이 실패한다", async () => {
  // Given
  state.brokenUnique = true;
  // When / Then
  await assert.rejects(runReactionUser(target,
    config({ CHAT_REACTION_MODE: "duplicate", CHAT_REACTION_ROUNDS: "1" }), {}), { reason: "mismatch" });
  assert.equal(count("failures"), 1);
});

test("거절 응답을 성공 ACK나 지연 표본으로 집계하지 않는다", async () => {
  // Given
  state.reject = true;
  // When / Then
  await assert.rejects(runReactionUser(target,
    config({ CHAT_REACTION_MODE: "duplicate", CHAT_REACTION_ROUNDS: "1" }), {}), { reason: "rejected" });
  assert.equal(count("successes"), 0);
  assert.equal(count("ack_latency_ms"), 0);
});

test("ACK가 유실되면 시간 초과를 실패로 기록하고 성공 지연 표본에는 넣지 않는다", async () => {
  // Given
  state.silentAdd = true;
  // When / Then
  await assert.rejects(runReactionUser(target,
    config({ CHAT_REACTION_MODE: "duplicate", CHAT_REACTION_ROUNDS: "1" }), {}), { reason: "timeout" });
  assert.equal(count("attempts"), 2);
  assert.equal(count("successes"), 0);
  assert.equal(count("failures"), 1);
});

test("준비된 메시지 ID는 다음 실행용 시드에 보존하고 재사용할 때 새로 만들지 않는다", async () => {
  // Given
  const settings = config();
  // When
  const prepared = await prepareReactionRun(seed, settings);
  const length = state.messages.length;
  const reused = await prepareReactionRun(prepared.reactionSeed, settings);
  // Then
  assert.equal(state.messages.length, length);
  assert.deepEqual(reused.plan.targets, prepared.plan.targets);
});

test("워밍업은 성능 표본에서 제외하고 측정 뒤 리액션 상태를 복구한다", async (t) => {
  // Given
  t.mock.timers.enable({ apis: ["Date", "setTimeout", "setInterval"], now: 1000 });
  const settings = config();
  const pending = runReactionUser(target, settings,
    { warmupStartAt: 1200, warmupEndAt: 1400, startAt: 1800, endAt: 2000 });
  // When
  for (let step = 0; step < 14; step++) {
    await new Promise(setImmediate);
    t.mock.timers.tick(100);
  }
  await pending;
  // Then
  assert.ok(count("warmup_attempts") > 0);
  assert.ok(count("attempts") > 0);
  assert.equal(count("attempts"), count("successes"));
  assert.equal(state.rows.length, 0);
  const metrics = Object.fromEntries(Object.entries(state.samples).map(([name, values]) =>
    [name, { values: { count: values.reduce((sum, item) => sum + Number(item.value), 0) } }]));
  const summary = reactionSummary({ metrics }, settings);
  assert.equal(summary.attemptedPerSecond, count("attempts") / 0.2);
});
