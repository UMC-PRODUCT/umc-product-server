import assert from "node:assert/strict";
import test from "node:test";
import { createLoadPlan, DeliveryTracker, loadContent } from "../lib/chat/chat-load.js";

const seed = { baseUrl: "http://localhost:8080", memberIds: ["1", "2", "3"], rooms: [
  { threadId: "10", memberIds: ["1", "2", "3"] }, { threadId: "20", memberIds: ["2", "3"] }] };
const config = { baseUrl: seed.baseUrl, users: 3, rooms: 2 };
const target = createLoadPlan(seed, config)[0];

function event(kind, sequence, senderId = "2", sentAt = 1000) {
  const message = { threadId: "10", senderId, clientMessageId: `id-${senderId}-${kind}-${sequence}` };
  const content = loadContent("run", kind, message, sentAt, sequence);
  return JSON.parse(JSON.stringify({ type: "message.created", threadId: "10",
    payload: { clientMessageId: message.clientMessageId, message: { ...message, content, type: "TEXT" } } }));
}

test("한 방의 여러 사용자와 여러 방에 참여한 사용자를 그대로 배정한다", () => {
  const plan = createLoadPlan(seed, config);
  assert.deepEqual(plan.map((member) => member.rooms.length), [1, 2, 2]);
  assert.equal(createLoadPlan({ ...seed, memberIds: ["1"],
    rooms: [{ threadId: "10", memberIds: ["1"] }] }, { ...config, users: 1, rooms: 1 }).length, 1);
});

test("인원·방 수 불일치와 중복·미가입 사용자를 연결 전에 거부한다", () => {
  assert.throws(() => createLoadPlan(seed, { ...config, users: 4 }), /일치/);
  assert.throws(() => createLoadPlan({ ...seed, baseUrl: "http://other" }, config), /BASE_URL/);
  assert.throws(() => createLoadPlan({ ...seed, memberIds: ["1", "1", "3"] }, config), /중복/);
  assert.throws(() => createLoadPlan({ ...seed, rooms: [seed.rooms[0], seed.rooms[0]] }, config), /방/);
  assert.throws(() => createLoadPlan({ ...seed, rooms: seed.rooms.map((room) =>
    ({ ...room, memberIds: ["2", "3"] })) }, config), /적어도/);
});

test("공유 객체 없이 JSON을 거친 전송 시각으로 수신 지연을 계산한다", () => {
  const tracker = new DeliveryTracker(target, "run", 100);
  assert.deepEqual(tracker.observe(event("message", 1), 1040),
    { duplicate: false, latencyMs: 40, late: false });
  assert.deepEqual(tracker.observe(event("message", 1), 1050), { duplicate: true });
  assert.equal(tracker.observe(event("message", 1, "1"), 1050), null);
});

test("수신 순서가 바뀌어도 중복을 제외하고 최종 누락을 계산한다", () => {
  const tracker = new DeliveryTracker(target, "run", 100);
  tracker.observe(event("message", 3), 1040);
  tracker.observe(event("message", 1), 1040);
  tracker.observe(event("done", 3), 1040);
  tracker.observe(event("done", 0, "1"), 1040);
  tracker.observe(event("done", 0, "3"), 1040);
  assert.deepEqual(tracker.summary(), { expected: 3, missing: 1, completionMissing: 0 });
  tracker.observe(event("message", 2), 1040);
  assert.deepEqual(tracker.summary(), { expected: 3, missing: 0, completionMissing: 0 });
  assert.deepEqual(tracker.observe(event("message", 2), 1045), { duplicate: true });
});

test("완료 신호 누락은 예상 건수를 임의로 만들지 않고 별도 실패로 구분한다", () => {
  const tracker = new DeliveryTracker(target, "run", 100);
  tracker.observe(event("done", 0, "1"), 1040);
  assert.deepEqual(tracker.summary(), { expected: 0, missing: 0, completionMissing: 2 });
});

test("허용 시간 초과·다른 실행·식별 정보 불일치를 구분한다", () => {
  const tracker = new DeliveryTracker(target, "run", 100);
  assert.equal(tracker.observe(event("message", 1), 1200).late, true);
  assert.equal(new DeliveryTracker(target, "other", 100).observe(event("message", 1), 1040), null);
  assert.throws(() => tracker.observe({ ...event("message", 2), threadId: "20" }, 1040), /식별/);
  assert.throws(() => tracker.observe(event("message", 2), 999), /식별/);
});

test("모든 참여자의 준비·대기 확인을 받기 전에는 부하를 시작하지 않는다", () => {
  const tracker = new DeliveryTracker(target, "run", 100);
  for (const memberId of ["1", "2", "3"]) tracker.observe(event("ready", 0, memberId), 1040);
  assert.equal(tracker.ready, true);
  assert.equal(tracker.armed, false);
  for (const memberId of ["1", "2", "3"]) tracker.observe(event("armed", 0, memberId), 1040);
  assert.equal(tracker.armed, true);
});
