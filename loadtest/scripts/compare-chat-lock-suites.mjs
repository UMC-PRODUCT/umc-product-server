import { readFileSync, writeFileSync } from "node:fs";
import { join, resolve } from "node:path";
import { compareRuns, ComparisonError } from "./compare-chat-runs.mjs";
import { readJson } from "./chat-suite-process.mjs";

function loadCase(entry) {
  if (entry?.status !== "completed") throw new ComparisonError("완료되지 않은 측정이 있습니다");
  const directory = join(entry.directory, "run");
  const validation = readFileSync(join(entry.directory, "consistency.jsonl"), "utf8").trim().split("\n").map(JSON.parse);
  if (validation.length !== 2 || validation.some((item) => item.passed !== true)) {
    throw new ComparisonError("DB 정합성 검증 두 단계를 통과하지 않았습니다");
  }
  const run = { conditions: readJson(join(directory, "conditions.json")),
    summary: readJson(join(directory, "summary.json")), meta: readJson(join(directory, "run-meta.json")) };
  if (!run.conditions.initialState || run.conditions.dataModel !== "lock-suite-v1") {
    throw new ComparisonError("초기 상태가 기록된 락 비교 결과가 아닙니다");
  }
  return run;
}

function observation(entry, run) {
  const { measurementStartAt: start, measurementEndAt: end } = run.conditions.timing;
  const samples = readFileSync(join(entry.directory, "locks.jsonl"), "utf8").split("\n")
    .filter((line) => line.startsWith("{"))
    .map(JSON.parse).filter((sample) => Date.parse(sample.sampleAt) >= start && Date.parse(sample.sampleAt) <= end);
  if (!samples.length || samples.some((sample) => sample.unreadableSessions !== 0)) {
    throw new ComparisonError("본 측정 구간의 DB 표본이 없거나 조회 권한이 부족합니다");
  }
  const waitsByQuery = {};
  for (const sample of samples) {
    for (const session of sample.sessions.filter((item) => item.waitEventType === "Lock")) {
      waitsByQuery[session.queryKind] = (waitsByQuery[session.queryKind] || 0) + 1;
    }
  }
  const grafana = new URL("https://grafana.university.neordinary.com/d/umc-product-k6-load-test/ebb680-ed9598-ed858c-ec8aa4-ed8ab8-k6");
  const pr = /api-pr-([0-9]+)/.exec(run.conditions.config.baseUrl)?.[1];
  for (const [key, value] of Object.entries({ from: start, to: end, timezone: "Asia/Seoul",
    "var-env": "preview", "var-service": "preview-umc-product-pr" + pr, "var-testid": run.conditions.comparison.testId })) {
    grafana.searchParams.set(key, String(value));
  }
  return { samples: samples.length, averageWaitingSessions: samples.reduce((sum, item) => sum + item.lockWaiters, 0) / samples.length,
    databaseBefore: readJson(join(entry.directory, "database-before.json")),
    maxWaitingSessions: Math.max(...samples.map((item) => item.lockWaiters)),
    fractionOfSamplesWithWait: samples.filter((item) => item.lockWaiters > 0).length / samples.length,
    waitingSessionSamplesByQuery: waitsByQuery, grafana: grafana.href, rawObservations: entry.directory,
    limit: "대기 세션의 표본 통계. 요청별 전체 락 대기시간이나 대기 건수가 아님" };
}

const [beforeDirectory, afterDirectory, outputPrefix] = process.argv.slice(2);
if (!beforeDirectory || !afterDirectory || !outputPrefix) {
  throw new ComparisonError("사용법: node compare-chat-lock-suites.mjs <baseline 폴더> <removed 폴더> <저장 파일 접두사>");
}
const before = readJson(join(resolve(beforeDirectory), "suite.json"));
const after = readJson(join(resolve(afterDirectory), "suite.json"));
if (before.inputs.variant !== "baseline" || after.inputs.variant !== "removed") throw new ComparisonError("전후 순서가 잘못됐습니다");
const comparisons = {};
const rows = [["scenario", "users", "operation", "before_p95_ms", "after_p95_ms", "before_p99_ms", "after_p99_ms",
  "before_success_per_second", "after_success_per_second", "before_error_rate", "after_error_rate"]];
for (const operation of ["reaction", "read", "join", "report", "mixed"]) {
  for (const users of [10, 50]) {
    const key = operation + "-" + users;
    const baseline = loadCase(before.cases[key]);
    const removed = loadCase(after.cases[key]);
    const comparison = compareRuns(baseline, removed);
    if (!comparison.allChecksPassed) throw new ComparisonError(key + "의 부하 검증이 실패했습니다");
    comparisons[key] = { ...comparison, observation: {
      baseline: observation(before.cases[key], baseline), removed: observation(after.cases[key], removed) } };
    for (const action of operation === "mixed" ? ["reaction_add", "reaction_remove", "read", "join", "report"]
      : operation === "reaction" ? ["reaction_add", "reaction_remove"] : [operation]) {
      const values = (run, metric) => run.summary.metrics[`${metric}{operation:${action}}`]?.values;
      const oldLatency = values(baseline, "umc_chat_lock_latency_ms");
      const newLatency = values(removed, "umc_chat_lock_latency_ms");
      if (!oldLatency || !newLatency || !values(baseline, "umc_chat_lock_successes")
          || !values(removed, "umc_chat_lock_successes")) throw new ComparisonError(key + ": 작업별 측정값이 없습니다");
      const rate = (run) => values(run, "umc_chat_lock_successes")?.count / (run.conditions.config.measurementMs / 1000);
      rows.push([operation, users, action, oldLatency?.["p(95)"], newLatency?.["p(95)"], oldLatency?.["p(99)"],
        newLatency?.["p(99)"], rate(baseline), rate(removed), values(baseline, "umc_chat_lock_error_rate")?.rate,
        values(removed, "umc_chat_lock_error_rate")?.rate]);
    }
  }
}
writeFileSync(outputPrefix + ".json", JSON.stringify({ comparisons,
  limits: ["각 조합은 한 번 측정한 결과이며 반복 측정의 변동 폭을 나타내지 않음",
    "동일 회차의 같은 방에 동시 요청. 회차마다 새 방을 사용하므로 한 방 장시간 누적 부하와 구분",
    "실행 전 DB 전체 행 수를 함께 확인. 새 시드의 초기 상태가 같아도 DB 전체 데이터 규모는 다를 수 있음",
    "리액션 두 락을 포함한 대상 락을 함께 제거한 기능별 결과. 각 락의 독립 기여도를 분리하지 않음"] }, null, 2) + "\n");
writeFileSync(outputPrefix + ".csv", rows.map((row) => row.join(",")).join("\n") + "\n");
console.log("10쌍 비교 완료: " + outputPrefix + ".json / .csv");
