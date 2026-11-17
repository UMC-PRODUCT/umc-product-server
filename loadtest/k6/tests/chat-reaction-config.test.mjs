import assert from "node:assert/strict";
import test from "node:test";
import { buildReactionOptions, createReactionPlan, readReactionConfig } from "../config/chat-reaction.js";

const seed = { baseUrl: "http://localhost:8080", memberIds: ["1", "2"],
  rooms: [{ threadId: "10", memberIds: ["1", "2"], messageIds: ["101", "102"] }] };

test("성능 모드는 서로 다른 메시지를 배정하고 변경한 시간 설정을 실행 제한에 반영한다", () => {
  // Given
  const config = readReactionConfig({ CHAT_USERS: "2", CHAT_REACTION_INTERVAL_MS: "250",
    CHAT_REACTION_WARMUP: "5s", DURATION: "45s", CHAT_STARTUP_TIMEOUT_MS: "500" });
  // When
  const plan = createReactionPlan(seed, config);
  const options = buildReactionOptions(config);
  // Then
  assert.deepEqual(plan.targets.map((target) => target.messageId), ["101", "102"]);
  assert.equal(config.intervalMs, 250);
  assert.equal(config.warmupMs, 5000);
  assert.equal(config.durationMs, 45000);
  assert.equal(options.scenarios.reactions.vus, 2);
  assert.ok(parseInt(options.scenarios.reactions.maxDuration) > config.durationMs + config.warmupMs);
});

test("이전 조건 파일로 실행하면 같은 회원과 메시지를 재사용한다", () => {
  // Given
  const recorded = { reactionSeed: seed, config: { token: "복사하지 않을 값" } };
  // When
  const plan = createReactionPlan(recorded, readReactionConfig({ CHAT_USERS: "2" }));
  // Then
  assert.deepEqual(plan.targets.map((target) => target.messageId), ["101", "102"]);
  assert.equal(JSON.stringify(plan).includes("복사하지 않을 값"), false);
});

test("중복 모드는 같은 시드에서 첫 회원 한 명만 선택하고 부하 VU를 늘리지 않는다", () => {
  // Given
  const config = readReactionConfig({ CHAT_REACTION_MODE: "duplicate", CHAT_USERS: "50", CHAT_REACTION_ROUNDS: "3" });
  // When
  const plan = createReactionPlan(seed, config);
  // Then
  assert.equal(plan.targets.length, 1);
  assert.equal(plan.targets[0].memberId, "1");
  assert.equal(config.rounds, 3);
  assert.equal(buildReactionOptions(config).scenarios.reactions.vus, 1);
});

test("대상 서버나 참여 인원과 메시지 배정이 다르면 요청 전에 거부한다", () => {
  // Given
  const config = readReactionConfig({ CHAT_USERS: "2" });
  const cases = [
    { ...seed, baseUrl: "http://other-host" },
    { ...seed, memberIds: ["1"] },
    { ...seed, rooms: [{ ...seed.rooms[0], memberIds: ["1", "1"] }] },
    { ...seed, rooms: [{ ...seed.rooms[0], messageIds: ["101", "101"] }] },
    { ...seed, rooms: [{ ...seed.rooms[0], messageIds: ["101"] }] },
    { ...seed, rooms: [...seed.rooms, seed.rooms[0]] },
  ];
  // When / Then
  for (const input of cases) assert.throws(() => createReactionPlan(input, config));
});

test("잘못된 모드와 시간은 거부하고 명시한 워밍업 생략은 허용한다", () => {
  // Given / When / Then
  for (const env of [{ CHAT_REACTION_MODE: "other" }, { CHAT_LOCK_VARIANT: "actual-toggle" },
    { CHAT_REACTION_INTERVAL_MS: "0" }, { CHAT_REACTION_INTERVAL_MS: "1" },
    { CHAT_REACTION_WARMUP: "-1s" }, { DURATION: "0s" }]) assert.throws(() => readReactionConfig(env));
  assert.equal(readReactionConfig({ CHAT_REACTION_WARMUP: "0s" }).warmupMs, 0);
});
