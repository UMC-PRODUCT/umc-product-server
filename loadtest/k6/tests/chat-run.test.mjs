import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync, readFileSync, readdirSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { spawnSync } from "node:child_process";
import test from "node:test";
import { chatRunConditions } from "../lib/chat/chat-run.js";

const seed = { baseUrl: "http://localhost:8080", memberIds: ["10", "20", "30"],
  rooms: [{ threadId: "1", memberIds: ["10", "20", "30"] }] };

test("새 시나리오의 추가 설정을 유지하고 실제 선택한 이름과 옵션을 기록한다", () => {
  const options = { scenarios: { custom: { vus: 2 } } };
  const snapshot = chatRunConditions({ profile: "load", scenario: "chat-custom", seed, options }, (data) => {
    assert.equal(data, seed);
    return { scenario: "잘못된 이름", profile: "다른 프로필", k6Options: {},
      config: { batchSize: 5 }, vuMemberIds: [["10", "30"], ["20"]],
      customMeasurement: { target: "request-order" } };
  });
  assert.equal(snapshot.scenario, "chat-custom");
  assert.equal(snapshot.profile, "load");
  assert.deepEqual(snapshot.k6Options, options);
  assert.equal(snapshot.config.batchSize, 5);
  assert.deepEqual(snapshot.customMeasurement, { target: "request-order" });
});

test("조건 기록 함수가 없거나 동기적인 조건 객체를 반환하지 않으면 거부한다", () => {
  const context = { profile: "load", scenario: "chat-custom", seed, options: {} };
  assert.throws(() => chatRunConditions(context), /getRunConditions/);
  for (const result of [null, [], "문자열", Promise.resolve({}), { available: false }]) {
    assert.throws(() => chatRunConditions(context, () => result), /동기적으로/);
  }
});

test("실행 래퍼는 성공·실패·요약 누락과 연속 실행의 기록을 각각 보존한다", () => {
  const fixture = mkdtempSync(join(tmpdir(), "chat-run-test-"));
  const script = resolve(import.meta.dirname, "../../scripts/run-chat-test.sh");
  const runs = [];
  const json = (directory, file) => JSON.parse(readFileSync(join(directory, file), "utf8"));
  try {
    writeFileSync(join(fixture, "seed.json"), JSON.stringify({ ...seed, token: "do-not-copy",
      rooms: [{ ...seed.rooms[0], token: "do-not-copy" }] }));
    writeFileSync(join(fixture, "k6"), `#!/usr/bin/env node
const fs = require("node:fs");
const path = require("node:path");
if (process.argv[2] === "version") { console.log("k6 test-double"); process.exit(0); }
const out = process.env.CHAT_RUN_DIR;
console.log("실행 로그 " + process.env.PROFILE + "/" + process.env.SCENARIO);
if (process.env.TEST_K6_MODE !== "missing") {
  fs.writeFileSync(path.join(out, "summary.json"), JSON.stringify({ metrics: {} }));
  fs.writeFileSync(path.join(out, "conditions.json"), JSON.stringify({
    profile: process.env.PROFILE, scenario: process.env.SCENARIO,
    available: process.env.TEST_K6_MODE !== "unavailable" }));
}
process.exit(process.env.TEST_K6_MODE === "failure" ? 99 : 0);
`, { mode: 0o755 });
    for (const [mode, scenario] of [["success", ""], ["success", "chat-custom"],
      ["failure", "chat-custom"], ["missing", ""], ["unavailable", ""]]) {
      const result = spawnSync("bash", [script, "test-archive"], {
        cwd: fixture, encoding: "utf8", env: { ...process.env,
          PATH: `${fixture}:${process.env.PATH}`, SEED_FILE: "seed.json", PROFILE: "load", SCENARIO: scenario,
          TEST_K6_MODE: mode, CHAT_RUN_NOTES: "로컬 DB, 데이터 규모 미확인", SECRET_TOKEN: "do-not-copy" },
      });
      const directory = /^결과 폴더: (.+)$/m.exec(result.stdout)?.[1];
      assert.ok(directory, result.stderr);
      runs.push(directory);
      assert.equal(result.status, mode === "failure" ? 99 : ["missing", "unavailable"].includes(mode) ? 1 : 0, result.stderr);
      const meta = json(directory, "run-meta.json");
      assert.equal(meta.exitCode, result.status);
      assert.equal(meta.k6ExitCode, mode === "failure" ? 99 : 0);
      assert.ok(meta.startedAt && meta.endedAt && meta.commit);
      assert.equal(meta.notes, "로컬 DB, 데이터 규모 미확인");
      assert.equal(meta.scenario, scenario || "chat-message");
      assert.ok(readFileSync(join(directory, "console.log"), "utf8").includes(`실행 로그 load/${meta.scenario}`));
      if (mode !== "missing") assert.equal(json(directory, "conditions.json").scenario, meta.scenario);
      assert.deepEqual(json(directory, "seed.json").memberIds, seed.memberIds);
      for (const file of readdirSync(directory)) {
        assert.equal(readFileSync(join(directory, file), "utf8").includes("do-not-copy"), false);
      }
    }
    assert.equal(new Set(runs).size, 5);
  } finally {
    for (const directory of runs) rmSync(directory, { recursive: true, force: true });
    rmSync(fixture, { recursive: true, force: true });
  }
});
