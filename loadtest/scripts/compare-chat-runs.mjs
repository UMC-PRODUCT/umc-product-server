import { readFileSync } from "node:fs";
import { resolve, join } from "node:path";
import { pathToFileURL } from "node:url";
import { isDeepStrictEqual } from "node:util";

export class ComparisonError extends Error {
  constructor(message) { super(message); this.name = "ComparisonError"; }
}

function settings(value) {
  if (Array.isArray(value)) return value.map(settings);
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.entries(value)
      .filter(([key]) => !["variant", "lockVariant", "serverRevision"].includes(key))
      .map(([key, item]) => [key, settings(item)]));
  }
  return value;
}

export function compareRuns(baseline, removed) {
  const before = baseline.conditions;
  const after = removed.conditions;
  for (const [run, variant] of [[baseline, "baseline"], [removed, "removed"]]) {
    const metadata = run.conditions?.comparison;
    if (!metadata || metadata.schemaVersion !== 1 || metadata.lockVariant !== variant
        || !/^[a-f0-9]{40}$/.test(metadata.serverRevision)
        || !metadata.harnessHash || !metadata.instrumentationHash) {
      throw new ComparisonError("비교용 실행의 버전, 배포 SHA, 코드 해시가 필요합니다");
    }
    if (!run.conditions.config || !run.summary?.metrics || !run.meta?.endedAt) {
      throw new ComparisonError("실행 조건이나 완료 기록이 없습니다");
    }
  }
  if (before.comparison.serverRevision === after.comparison.serverRevision) {
    throw new ComparisonError("두 실행에 같은 배포 SHA가 기록되어 있습니다. 실제 배포 버전을 확인하세요");
  }
  if (baseline.meta.k6Version !== removed.meta.k6Version) {
    throw new ComparisonError("k6 버전이 다릅니다. 같은 부하 발생기를 사용하세요");
  }
  for (const key of ["harnessHash", "instrumentationHash"]) {
    if (before.comparison[key] !== after.comparison[key]) {
      throw new ComparisonError("두 실행의 부하 코드 또는 서버 계측 코드가 다릅니다: " + key);
    }
  }
  const targets = (run) => ({ profile: run.profile, scenario: run.scenario, config: settings(run.config),
    rooms: run.rooms, targets: run.targets, reactionSeed: run.reactionSeed,
    vuMemberIds: run.vuMemberIds, senderMemberIds: run.senderMemberIds });
  if (!isDeepStrictEqual(targets(before), targets(after))) {
    throw new ComparisonError("부하 설정 또는 방, 회원, 대상 메시지가 다릅니다. 동일한 시드를 재사용하세요");
  }
  const names = [...new Set([...Object.keys(baseline.summary.metrics), ...Object.keys(removed.summary.metrics)])]
    .filter((name) => name.startsWith("umc_chat_") || name === "checks" || name === "dropped_iterations");
  const metrics = Object.fromEntries(names.map((name) => [name, {
    baseline: baseline.summary.metrics[name]?.values ?? null,
    removed: removed.summary.metrics[name]?.values ?? null,
  }]));
  return { comparableConditions: true,
    testIds: [before.comparison.testId, after.comparison.testId],
    revisions: [before.comparison.serverRevision, after.comparison.serverRevision],
    exitCodes: [baseline.meta.exitCode, removed.meta.exitCode],
    allChecksPassed: baseline.meta.exitCode === 0 && removed.meta.exitCode === 0,
    measurementWindows: [before.timing || before.measurement, after.timing || after.measurement],
    measuredResults: [before.measuredResults || before.measurement, after.measuredResults || after.measurement],
    metrics,
    limits: ["코드 해시는 실행 당시 로컬 파일 기준입니다. 실제 배포 SHA와 런타임 설정은 별도로 확인하세요.",
      "실패율과 지연을 함께 비교하세요. 성공 응답의 p99만으로 전체 요청을 평가하지 마세요.",
      "서버 메트릭과 DB 표본은 본 측정 시간 구간으로 대조하세요. DB 표본으로 요청별 락 대기 p99를 만들지 마세요."] };
}

function readRun(directory) {
  const json = (name) => JSON.parse(readFileSync(join(directory, name), "utf8"));
  return { conditions: json("conditions.json"), summary: json("summary.json"), meta: json("run-meta.json") };
}

if (process.argv[1] && pathToFileURL(resolve(process.argv[1])).href === import.meta.url) {
  if (process.argv.length !== 4) throw new ComparisonError("사용법: node loadtest/scripts/compare-chat-runs.mjs <baseline 폴더> <removed 폴더>");
  process.stdout.write(JSON.stringify(compareRuns(readRun(process.argv[2]), readRun(process.argv[3])), null, 2) + "\n");
}
