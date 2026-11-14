import { mkdirSync, existsSync, openSync, closeSync, unlinkSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { setTimeout as pause } from "node:timers/promises";
import { readLockConfig } from "../k6/config/chat-locks.js";
import { SuiteError, readJson, saveJson, runProcess, databaseState, observeRun,
  interruptSuite, interruption } from "./chat-suite-process.mjs";

const scripts = dirname(fileURLToPath(import.meta.url));
const [variant, output, flag] = process.argv.slice(2);
if (variant === "--help" || !variant) {
  console.log("사용법: node loadtest/scripts/run-chat-lock-suite.mjs baseline|removed <결과 폴더> [--plan]");
  console.log("같은 폴더로 재실행하면 완료 구간을 건너뛰고, 실패 구간은 새 데이터로 다시 실행합니다.");
  console.log("필수: BASE_URL, CHAT_SERVER_REVISION, TEST_API_BASIC_AUTH");
  console.log("CHAT_SERVER_REVISION은 직접 확인한 배포 커밋입니다. 실제 배포 상태는 자동 조회하지 않습니다.");
  console.log("DB: PGDATABASE, PGHOST 또는 PGSERVICE, PGUSER, PGPASSFILE, 필요하면 PSQL_BIN");
  console.log("설정: CHAT_WARMUP=30s DURATION=2m CHAT_LOCK_INTERVAL_MS=2500 CHAT_COOLDOWN_SECONDS=30");
  console.log("Grafana k6 전송: CHAT_K6_OUTPUT=experimental-prometheus-rw 및 K6_PROMETHEUS_RW_SERVER_URL 설정");
  console.log("서버 지표는 conditions.json의 본 측정 시간에 맞춰 Grafana에서 직접 확인합니다.");
  console.log("신고 전제: 프리뷰의 Discord 신고 전달을 끄고 CHAT_REPORT_DELIVERY_DISABLED=1 지정");
  process.exit(0);
}
if (!["baseline", "removed"].includes(variant) || !output || (flag && flag !== "--plan")) {
  throw new SuiteError("variant, 결과 폴더, 선택 --plan 인자를 확인하세요.");
}
const directory = resolve(output);
const configurations = ["reaction", "read", "join", "report", "mixed"].flatMap((operation) => [10, 50].map((users) =>
  readLockConfig({ ...process.env, CHAT_LOCK_OPERATION: operation, CHAT_USERS: String(users) })));
const cooldown = Number(process.env.CHAT_COOLDOWN_SECONDS || 30);
if (!Number.isInteger(cooldown) || cooldown < 10 || cooldown > 600) throw new SuiteError("휴식은 10~600초로 지정하세요.");
if (flag === "--plan") {
  console.log(JSON.stringify({ variant, directory, cooldownSeconds: cooldown,
    measurements: configurations.map((config) => ({ operation: config.operation, users: config.users,
      warmupRounds: config.warmupRounds, measurementRounds: config.measurementRounds,
      intervalMs: config.intervalMs, roles: config.roles })) }, null, 2));
  process.exit(0);
}

async function main() {
  const env = { ...process.env, CHAT_LOCK_VARIANT: variant, CHAT_COMPARISON: "1", PROFILE: "load", SCENARIO: "chat-locks" };
  for (const key of ["BASE_URL", "CHAT_SERVER_REVISION", "TEST_API_BASIC_AUTH"]) {
    if (!env[key]) throw new SuiteError(key + " 설정이 필요합니다.");
  }
  const preview = /^https:\/\/api-pr-([0-9]+)\.preview\.university\.neordinary\.com$/.exec(env.BASE_URL);
  if (!preview || env.PGDATABASE !== "umc_product_pr" + preview[1]
      || !/^[a-f0-9]{40}$/.test(env.CHAT_SERVER_REVISION)) throw new SuiteError("프리뷰 주소, DB, 배포 SHA가 일치해야 합니다.");
  if (env.CHAT_REPORT_DELIVERY_DISABLED !== "1") throw new SuiteError("프리뷰의 신고 운영 채널 전달 설정을 먼저 확인하세요.");
  mkdirSync(directory, { recursive: true });
  const guard = join(directory, ".running");
  const descriptor = openSync(guard, "wx", 0o600);
  closeSync(descriptor);
  try {
    const path = join(directory, "suite.json");
    const versions = {};
    for (const command of ["node", "k6"]) versions[command] = (await runProcess(command, ["--version"])).trim();
    const head = (await runProcess("git", ["-C", scripts, "rev-parse", "HEAD"])).trim();
    const dirty = await runProcess("git", ["-C", scripts, "status", "--porcelain", "--untracked-files=all",
      "--", "../../monolith/src/main", "../../app", "../../loadtest/k6", "../../loadtest/scripts"]);
    if (head !== env.CHAT_SERVER_REVISION || dirty.trim()) {
      throw new SuiteError("로컬 코드와 배포 커밋이 같고, 서버와 부하 코드에 미커밋 변경이 없어야 실행할 수 있습니다.");
    }
    const inputs = { schemaVersion: 2, variant, revision: env.CHAT_SERVER_REVISION,
      configurations, cooldown, versions, database: env.PGDATABASE,
      serverMetrics: "grafana-manual", lockInterval: env.LOCK_WATCH_INTERVAL_MS || "200" };
    const state = existsSync(path) ? readJson(path) : { inputs, cases: {} };
    if (JSON.stringify(state.inputs) !== JSON.stringify(inputs)) throw new SuiteError("재개 조건이 다릅니다. 새 결과 폴더를 지정하세요.");
    saveJson(path, state);
    await runProcess("bash", [join(scripts, "watch-reaction-locks.sh")], {
      env: { ...env, LOCK_WATCH_SAMPLES: "1" }, log: join(directory, "db-preflight.jsonl") });
    for (const [index, config] of configurations.entries()) {
      interruption.signal.throwIfAborted();
      const key = config.operation + "-" + config.users;
      if (state.cases[key]?.status === "completed") { console.log("완료 구간 유지: " + key); continue; }
      const attempt = (state.cases[key]?.attempt || 0) + 1;
      const attemptDir = join(directory, key + "-attempt-" + attempt);
      mkdirSync(attemptDir);
      const testId = "locks-" + variant + "-" + key + "-" + Date.now();
      const caseEnv = { ...env, CHAT_LOCK_OPERATION: config.operation, CHAT_USERS: String(config.users),
        CHAT_TEST_ID: testId, CHAT_SERVICE: "preview-umc-product-pr" + preview[1],
        SEED_FILE: join(attemptDir, "seed.json"), CHAT_RUN_DIR: join(attemptDir, "run") };
      state.cases[key] = { status: "running", attempt, directory: attemptDir, testId };
      saveJson(path, state);
      console.log(`[${index + 1}/10] ${key} 데이터 준비와 측정 시작. 로그: ${attemptDir}`);
      try {
        saveJson(join(attemptDir, "database-before.json"), await databaseState(caseEnv));
        await runProcess("bash", [join(scripts, "prepare-chat-data.sh")], {
          env: { ...caseEnv, SUT_URL: env.BASE_URL, CHAT_USERS: String(config.users * 2 + 1),
            CHAT_ROOMS: "1", CHAT_ROOM_MEMBERS: "", CHAT_SEED_MEMBERS_ONLY: "1", CHAT_SEED_OUTPUT: caseEnv.SEED_FILE },
          log: join(attemptDir, "prepare.log") });
        await observeRun({ env: caseEnv, directory: attemptDir, scripts });
        await runProcess("bash", [join(scripts, "verify-chat-consistency.sh"), join(caseEnv.CHAT_RUN_DIR, "conditions.json")],
          { env: caseEnv, log: join(attemptDir, "consistency.jsonl") });
        state.cases[key].status = "completed";
        saveJson(path, state);
        console.log(`[${index + 1}/10] ${key} 완료. ${cooldown}초 휴식`);
      } catch (error) {
        state.cases[key].status = "failed";
        saveJson(path, state);
        throw error;
      }
      await pause(cooldown * 1000, undefined, { signal: interruption.signal });
    }
    console.log("10개 측정 완료: " + path);
    if (variant === "removed" && env.CHAT_BASELINE_SUITE) {
      await runProcess(process.execPath, [join(scripts, "compare-chat-lock-suites.mjs"),
        env.CHAT_BASELINE_SUITE, directory, join(directory, "comparison")]);
      console.log("전후 비교 저장: " + join(directory, "comparison.json"));
    }
  } finally { unlinkSync(guard); }
}

process.on("SIGINT", interruptSuite);
process.on("SIGTERM", interruptSuite);
main().catch((error) => {
  if (!(error instanceof Error)) throw error;
  console.error("[오류] " + error.message);
  process.exitCode = 1;
}).finally(() => {
  process.removeListener("SIGINT", interruptSuite);
  process.removeListener("SIGTERM", interruptSuite);
});
