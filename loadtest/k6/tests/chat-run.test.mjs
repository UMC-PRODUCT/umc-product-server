import assert from "node:assert/strict";
import { mkdtempSync, writeFileSync, readFileSync, readdirSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { spawnSync } from "node:child_process";
import test from "node:test";
import { chatRunConditions } from "../lib/chat/chat-run.js";

const seed = { baseUrl: "http://localhost:8080", memberIds: ["10", "20", "30"],
  rooms: [{ threadId: "1", memberIds: ["10", "20", "30"] }] };

test("실행 조건에는 기본값·실제 회원 배정이 포함되고 인증 정보는 제외한다", () => {
  const snapshot = chatRunConditions("load", { CHAT_USERS: "3", CHAT_VU_USERS: "[[1,3],[2]]",
    CHAT_SENDERS: "[2]", SECRET_TOKEN: "secret" }, seed, { scenarios: {} });
  assert.equal(snapshot.config.intervalMs, 1000);
  assert.equal(snapshot.config.durationMs, 30000);
  assert.deepEqual(snapshot.vuMemberIds, [["10", "30"], ["20"]]);
  assert.deepEqual(snapshot.senderMemberIds, ["20"]);
  assert.equal(JSON.stringify(snapshot).includes("secret"), false);
});

test("Smoke 실행 조건은 전체 시드와 무관하게 첫 방의 두 회원을 기록한다", () => {
  const snapshot = chatRunConditions("smoke", {}, { ...seed,
    rooms: [{ threadId: "2", memberIds: ["20", "30"] }, seed.rooms[0]] }, {});
  assert.deepEqual(snapshot.vuMemberIds, [["20", "30"]]);
  assert.deepEqual(snapshot.senderMemberIds, ["20"]);
  assert.equal(snapshot.rooms.length, 1);
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
console.log("실행 로그 " + process.env.PROFILE);
if (process.env.TEST_K6_MODE !== "missing") {
  fs.writeFileSync(path.join(out, "summary.json"), JSON.stringify({ metrics: {} }));
  fs.writeFileSync(path.join(out, "conditions.json"), JSON.stringify({
    profile: process.env.PROFILE, available: process.env.TEST_K6_MODE !== "unavailable" }));
}
process.exit(process.env.TEST_K6_MODE === "failure" ? 99 : 0);
`, { mode: 0o755 });
    for (const mode of ["success", "success", "failure", "missing", "unavailable"]) {
      const result = spawnSync("bash", [script, "test-archive"], {
        cwd: fixture, encoding: "utf8", env: { ...process.env,
          PATH: `${fixture}:${process.env.PATH}`, SEED_FILE: "seed.json", PROFILE: "load",
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
      assert.match(readFileSync(join(directory, "console.log"), "utf8"), /실행 로그 load/);
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
