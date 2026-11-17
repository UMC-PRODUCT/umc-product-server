import { mkdirSync, existsSync, openSync, closeSync, unlinkSync, readFileSync } from "node:fs";
import { createHash } from "node:crypto";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { setTimeout as pause } from "node:timers/promises";
import { readLockConfig, lockSeed } from "../k6/config/chat-locks.js";
import { SuiteError, readJson, saveJson, runProcess, databaseState, observeRun,
  interruptSuite, interruption } from "./chat-suite-process.mjs";

const scripts = dirname(fileURLToPath(import.meta.url));
const [variant, output, flag] = process.argv.slice(2);
if (variant === "--help" || !variant) {
  console.log("사용법: node loadtest/scripts/run-chat-lock-suite.mjs baseline|removed <결과 폴더> [--plan]");
  console.log("같은 폴더로 재실행하면 완료 구간을 건너뛰고, 실패 구간은 새 데이터로 다시 실행합니다.");
  console.log("필수: BASE_URL, CHAT_SERVER_REVISION, TEST_API_BASIC_AUTH");
  console.log("CHAT_SERVER_REVISION은 직접 확인한 배포 커밋입니다. 실제 배포 상태는 자동 조회하지 않습니다.");
  console.log("회원 101명을 한 번 가입시켜 재사용합니다. APP_TEST_API_ENABLED=true가 필요하며 시딩 API는 사용하지 않습니다.");
  console.log("CHAT_REUSE_MEMBERS_FROM=<기존 suite의 members.json>으로 전후에도 같은 회원을 사용할 수 있습니다.");
  console.log("선택 실행: CHAT_LOCK_CASES=read-50,mixed-50 (생략하면 전체 10개)");
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
const allConfigurations = ["reaction", "read", "join", "report", "mixed"].flatMap((operation) => [10, 50].map((users) =>
  readLockConfig({ ...process.env, CHAT_LOCK_OPERATION: operation, CHAT_USERS: String(users) })));
const selectedCases = process.env.CHAT_LOCK_CASES?.split(",");
if (selectedCases && (new Set(selectedCases).size !== selectedCases.length
    || selectedCases.some((key) => !allConfigurations.some((config) => key === config.operation + "-" + config.users)))) {
  throw new SuiteError("CHAT_LOCK_CASES에 지원하는 시나리오와 인원을 중복 없이 지정하세요.");
}
const configurations = allConfigurations.filter((config) => !selectedCases
  || selectedCases.includes(config.operation + "-" + config.users));
const poolUsers = Math.max(...configurations.map((config) => config.users));
const poolConfig = readLockConfig({ ...process.env, CHAT_USERS: String(poolUsers) });
const cooldown = Number(process.env.CHAT_COOLDOWN_SECONDS || 30);
if (!Number.isInteger(cooldown) || cooldown < 10 || cooldown > 600) throw new SuiteError("휴식은 10~600초로 지정하세요.");
if (flag === "--plan") {
  console.log(JSON.stringify({ variant, directory, cooldownSeconds: cooldown, memberPoolSize: poolUsers * 2 + 1,
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
    const serverPaths = ["../../monolith/src/main", "../../blog/src/main", "../../app", "../../gradle",
      "../../build.gradle.kts", "../../settings.gradle.kts", "../../gradle.properties", "../../Dockerfile"];
    const serverDiff = await runProcess("git", ["-C", scripts, "diff", "--name-only", env.CHAT_SERVER_REVISION,
      "--", ...serverPaths]);
    const untrackedServer = await runProcess("git", ["-C", scripts, "ls-files", "--others", "--exclude-standard",
      "--", ...serverPaths]);
    if (serverDiff.trim() || untrackedServer.trim()) {
      throw new SuiteError("로컬 서버 코드가 지정한 배포 커밋과 다릅니다. 부하 스크립트만 로컬에서 수정할 수 있습니다.");
    }
    const harnessFiles = (await runProcess("git", ["-C", scripts, "ls-files", "-co", "--exclude-standard",
      "--", "../../loadtest/k6", "../../loadtest/scripts"])).trim().split("\n");
    const hash = createHash("sha256");
    for (const file of [...new Set(harnessFiles)].sort()) {
      hash.update(file + "\0").update(readFileSync(resolve(scripts, file))).update("\0");
    }
    const inputs = { schemaVersion: 3, variant, revision: env.CHAT_SERVER_REVISION, harnessHash: hash.digest("hex"),
      configurations, cooldown, versions, database: env.PGDATABASE,
      serverMetrics: "grafana-manual", lockInterval: env.LOCK_WATCH_INTERVAL_MS || "200" };
    const state = existsSync(path) ? readJson(path) : { inputs, cases: {} };
    if (JSON.stringify(state.inputs) !== JSON.stringify(inputs)) throw new SuiteError("재개 조건이 다릅니다. 새 결과 폴더를 지정하세요.");
    saveJson(path, state);
    await runProcess("bash", [join(scripts, "watch-reaction-locks.sh")], {
      env: { ...env, LOCK_WATCH_SAMPLES: "1" }, log: join(directory, "db-preflight.jsonl") });
    const poolPath = join(directory, "members.json");
    if (!existsSync(poolPath)) {
      if (env.CHAT_REUSE_MEMBERS_FROM) {
        const previous = readJson(resolve(env.CHAT_REUSE_MEMBERS_FROM));
        lockSeed(previous, poolConfig);
        saveJson(poolPath, previous);
      } else {
        console.log(`공통 테스트 회원 ${poolUsers * 2 + 1}명 준비. 로그: ${join(directory, "prepare-members.log")}`);
        await runProcess("bash", [join(scripts, "prepare-chat-data.sh")], {
          env: { ...env, SUT_URL: env.BASE_URL, CHAT_USERS: String(poolUsers * 2 + 1), CHAT_ROOMS: "1",
            CHAT_ROOM_MEMBERS: "", CHAT_SEED_MEMBERS_ONLY: "1", CHAT_SEED_OUTPUT: poolPath },
          log: join(directory, "prepare-members.log") });
      }
    }
    const pool = readJson(poolPath);
    const members = lockSeed(pool, poolConfig);
    if (typeof pool.runId !== "string" || !/^[a-f0-9-]{36}$/.test(pool.runId)
        || typeof pool.schoolId !== "string" || !/^[1-9][0-9]*$/.test(pool.schoolId)) {
      throw new SuiteError("회원 시드의 실행 ID와 학교 ID가 올바르지 않습니다.");
    }
    // DB 재생성 뒤 같은 숫자 ID가 다른 회원을 가리키는 경우도 거른다.
    const expectedMembers = pool.memberIds.map((id, index) => `(${id},'chat-${pool.runId}-${index + 1}@example.com')`);
    const validMembers = await runProcess(env.PSQL_BIN || "psql", ["-X", "--no-password", "--dbname=" + env.PGDATABASE,
      "--quiet", "--no-align", "--tuples-only", "--set=ON_ERROR_STOP=1", "-c",
      `SELECT count(*) FROM member m JOIN (VALUES ${expectedMembers.join(",")}) AS e(id,email)
       ON m.id=e.id AND m.email=e.email WHERE m.status='ACTIVE' AND m.school_id=${pool.schoolId}`], {
      env: { ...env, PGOPTIONS: (env.PGOPTIONS || "") + " -c default_transaction_read_only=on -c statement_timeout=10000" },
    });
    if (Number(validMembers.trim()) !== poolUsers * 2 + 1) {
      throw new SuiteError("준비한 테스트 회원이 현재 DB와 일치하지 않습니다. 새 결과 폴더에서 회원을 다시 준비하세요.");
    }
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
      console.log(`[${index + 1}/${configurations.length}] ${key} 데이터 준비와 측정 시작. 로그: ${attemptDir}`);
      try {
        saveJson(join(attemptDir, "database-before.json"), await databaseState(caseEnv));
        const caseSeed = { ...pool, memberIds: [...members.actors.slice(0, config.users), members.author,
          ...members.invitees.slice(0, config.users)] };
        lockSeed(caseSeed, config);
        saveJson(caseEnv.SEED_FILE, caseSeed);
        await observeRun({ env: caseEnv, directory: attemptDir, scripts });
        await runProcess("bash", [join(scripts, "verify-chat-consistency.sh"), join(caseEnv.CHAT_RUN_DIR, "conditions.json")],
          { env: caseEnv, log: join(attemptDir, "consistency.jsonl") });
        state.cases[key].status = "completed";
        saveJson(path, state);
        console.log(`[${index + 1}/${configurations.length}] ${key} 완료. ${cooldown}초 휴식`);
      } catch (error) {
        state.cases[key].status = "failed";
        saveJson(path, state);
        throw error;
      }
      await pause(cooldown * 1000, undefined, { signal: interruption.signal });
    }
    console.log(configurations.length + "개 측정 완료: " + path);
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
