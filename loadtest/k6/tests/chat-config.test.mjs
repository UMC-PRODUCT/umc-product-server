import assert from "node:assert/strict";
import test from "node:test";
import { durationMs, readChatConfig } from "../config/chat.js";
import { readPairConfig, buildPairOptions } from "../config/chat-pair.js";
import { readLoadConfig, buildLoadOptions } from "../config/chat-load.js";

test("Smoke와 반복 부하의 지표 이름은 환경변수 또는 명시한 시나리오 이름을 따른다", () => {
  for (const read of [readPairConfig, readLoadConfig]) {
    assert.equal(read({}).tags.scenario, "chat-message");
    assert.equal(read({ SCENARIO: "chat-custom" }).tags.scenario, "chat-custom");
    assert.equal(read({ SCENARIO: "chat-custom" }, "chat-explicit").tags.scenario, "chat-explicit");
  }
});

test("반복 부하의 사용자 수와 방 수는 서로 독립적이다", () => {
  const config = readLoadConfig({ CHAT_USERS: "4", CHAT_ROOMS: "1", DURATION: "1m30s",
    CHAT_MESSAGE_INTERVAL_MS: "250" });
  assert.equal(config.users, 4);
  assert.equal(config.rooms, 1);
  assert.equal(config.durationMs, 90000);
  assert.equal(config.intervalMs, 250);
  const options = buildLoadOptions(config);
  assert.equal(options.scenarios.chat_load.vus, 4);
  assert.deepEqual(options.thresholds.iterations, ["count==4"]);
  assert.ok(Number.parseInt(options.scenarios.chat_load.maxDuration) > 90000);
});

test("기본은 모두 송신하고 송신자 목록을 지정하면 나머지는 수신한다", () => {
  assert.deepEqual(readLoadConfig({ CHAT_USERS: "4" }).senders, [1, 2, 3, 4]);
  const config = readLoadConfig({ CHAT_USERS: "4", CHAT_SENDERS: "[1,3]", CHAT_VU_USERS: "[[1],[2,3,4]]" });
  assert.deepEqual(config.senders, [1, 3]);
  assert.deepEqual(config.vuUsers, [[1], [2, 3, 4]]);
  for (const value of ["", "null", "[]", "{}", "[1,1]", "[0]", "[5]", "[1.5]", '["1"]']) {
    assert.throws(() => readLoadConfig({ CHAT_USERS: "4", CHAT_SENDERS: value }), /CHAT_SENDERS/);
  }
});

test("사용자 다섯 명을 VU당 두 명씩 배정하면 2·2·1명과 세 VU가 된다", () => {
  const config = readLoadConfig({ CHAT_USERS: "5", CHAT_USERS_PER_VU: "2" });
  assert.deepEqual(config.vuUsers, [[1, 2], [3, 4], [5]]);
  const options = buildLoadOptions(config);
  assert.equal(options.scenarios.chat_load.vus, 3);
  assert.deepEqual(options.thresholds.iterations, ["count==3"]);
});

test("시나리오의 명시적 배정은 VU별 인원과 사용자 순서를 자유롭게 정한다", () => {
  const config = readLoadConfig({ CHAT_USERS: "5", CHAT_VU_USERS: "[[1,3],[2,4,5]]" });
  assert.deepEqual(config.vuUsers, [[1, 3], [2, 4, 5]]);
  assert.equal(buildLoadOptions(config).scenarios.chat_load.vus, 2);
  const single = readLoadConfig({ CHAT_USERS: "4", CHAT_USERS_PER_VU: "10" });
  assert.deepEqual(single.vuUsers, [[1, 2, 3, 4]]);
});

test("잘못된 배정·회원 중복·누락·두 배정 방식의 혼용은 거부한다", () => {
  for (const value of ["", "null", "[]", "{}", "[[1],[]]", "[[0,1]]", "[[1,3]]",
    "[[1,1]]", "[[1]]", "[[1.5,2]]", '[["1",2]]']) {
    assert.throws(() => readLoadConfig({ CHAT_USERS: "2", CHAT_VU_USERS: value }), /CHAT_VU_USERS/);
  }
  assert.throws(() => readLoadConfig({ CHAT_VU_USERS: "[[1,2]]", CHAT_USERS_PER_VU: "2" }), /하나만/);
});

test("Smoke는 반복 부하 설정과 관계없이 한 VU에서 한 번 실행한다", () => {
  const config = readPairConfig({ CHAT_USERS: "bad", CHAT_ROOMS: "bad", CHAT_USERS_PER_VU: "bad",
    CHAT_VU_USERS: "bad", CHAT_SENDERS: "bad", CHAT_MESSAGE_INTERVAL_MS: "bad", DURATION: "bad" });
  assert.deepEqual(config.tags, { scenario: "chat-message", profile: "smoke" });
  const options = buildPairOptions(config);
  assert.deepEqual(options.scenarios.chat_smoke, {
    executor: "per-vu-iterations", vus: 1, iterations: 1, maxDuration: "52000ms",
  });
  assert.deepEqual(options.thresholds.iterations, ["count==1"]);
});

test("연결 설정에는 사용자와 방 배정이 포함되지 않는다", () => {
  assert.deepEqual(readChatConfig({}), { baseUrl: "http://localhost:8080", timeoutMs: 10000 });
});

for (const [profile, read, build] of [["Smoke", readPairConfig, buildPairOptions],
  ["반복 부하", readLoadConfig, buildLoadOptions]]) {
  test(`${profile}는 성능 기준을 생략하면 기존 판정만 유지하고 p99도 요약한다`, () => {
    const config = read({});
    const options = build(config);
    assert.deepEqual(config.latencyThresholds, {});
    assert.equal(options.thresholds.umc_chat_ack_latency_ms, undefined);
    assert.equal(options.thresholds.umc_chat_delivery_latency_ms, undefined);
    assert.deepEqual(options.thresholds.checks, ["rate==1"]);
    assert.deepEqual(options.thresholds.umc_chat_failures, ["count==0"]);
    assert.ok(options.summaryTrendStats.includes("p(99)"));
  });

  test(`${profile}는 지정한 지연 기준만 추가하고 응답 대기 시간은 유지한다`, () => {
    for (const [name, metric, percentile] of [
      ["CHAT_ACK_P95_MS", "umc_chat_ack_latency_ms", 95],
      ["CHAT_ACK_P99_MS", "umc_chat_ack_latency_ms", 99],
      ["CHAT_DELIVERY_P95_MS", "umc_chat_delivery_latency_ms", 95],
      ["CHAT_DELIVERY_P99_MS", "umc_chat_delivery_latency_ms", 99],
    ]) {
      const config = read({ [name]: "250.5", CHAT_TIMEOUT_MS: "10000" });
      const expected = { [metric]: [`p(${percentile})<250.5`] };
      assert.equal(config.timeoutMs, 10000);
      assert.deepEqual(config.latencyThresholds, expected);
      const thresholds = build(config).thresholds;
      assert.deepEqual(Object.fromEntries(Object.entries(thresholds)
        .filter(([key]) => key.endsWith("_latency_ms"))), expected);
    }
  });

  test(`${profile}는 ACK와 전달의 p95·p99 기준을 함께 적용한다`, () => {
    const options = build(read({ CHAT_ACK_P95_MS: "100", CHAT_ACK_P99_MS: "200",
      CHAT_DELIVERY_P95_MS: "500", CHAT_DELIVERY_P99_MS: "1000" }));
    assert.deepEqual(options.thresholds.umc_chat_ack_latency_ms, ["p(95)<100", "p(99)<200"]);
    assert.deepEqual(options.thresholds.umc_chat_delivery_latency_ms, ["p(95)<500", "p(99)<1000"]);
  });

  test(`${profile}는 잘못된 성능 기준을 조용히 무시하지 않고 거부한다`, () => {
    for (const name of ["CHAT_ACK_P95_MS", "CHAT_ACK_P99_MS", "CHAT_DELIVERY_P95_MS", "CHAT_DELIVERY_P99_MS"]) {
      for (const value of ["", " ", "0", "-1", "NaN", "Infinity", "500ms", "1x", "1e3"]) {
        assert.throws(() => read({ [name]: value }), new RegExp(name));
      }
    }
  });
}

test("기간은 단위가 있는 양수만 허용한다", () => {
  assert.equal(durationMs("0.5s"), 500);
  assert.equal(durationMs("24h"), 86400000);
  for (const value of ["", "30", "0s", "-1s", "1sx", "25h", "1e3s", "0.0001ms"]) {
    assert.throws(() => durationMs(value), /DURATION/);
  }
});

for (const name of ["CHAT_USERS", "CHAT_ROOMS", "CHAT_USERS_PER_VU", "CHAT_MESSAGE_INTERVAL_MS", "CHAT_TIMEOUT_MS", "CHAT_STARTUP_TIMEOUT_MS"]) {
  test(`${name}의 잘못된 값은 보정하지 않고 거부한다`, () => {
    for (const value of ["", "0", "-1", "1.5", "1x", "NaN", "9999999999999999999999"]) {
      assert.throws(() => readLoadConfig({ [name]: value }), new RegExp(name));
    }
  });
}

test("잘못된 서버 주소를 거부한다", () => {
  for (const baseUrl of ["localhost:8080", "file:///tmp", "http://localhost:8080/path", "http://host?x=1"]) {
    assert.throws(() => readLoadConfig({ BASE_URL: baseUrl }), /BASE_URL/);
  }
});
