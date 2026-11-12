import { scenario } from "k6/execution";

import { buildReactionOptions, createReactionPlan, readReactionConfig } from "../../config/chat-reaction.js";
import { prepareReactionRun, runReactionUser } from "../../lib/chat/reaction-run.js";
import { reactionSummary } from "../../lib/chat/reaction-metrics.js";

export const requiresSeed = true;
export const getOptions = () => buildReactionOptions(readReactionConfig(__ENV));
export const validateSeed = (seed) => createReactionPlan(seed, readReactionConfig(__ENV));
export const prepareRun = (seed) => prepareReactionRun(seed, readReactionConfig(__ENV));

export function getRunConditions(seed, run, summary) {
  const config = readReactionConfig(__ENV);
  const plan = run?.plan || createReactionPlan(seed, config);
  return { config, targets: plan.targets, reactionSeed: run?.reactionSeed || null,
    timingValidation: "negative-latency-rejected",
    timing: run ? { warmupStartAt: run.warmupStartAt, warmupEndAt: run.warmupEndAt,
      measurementStartAt: run.startAt, measurementEndAt: run.endAt } : null,
    measurement: reactionSummary(summary, config),
    expectedFinalReactionCount: config.mode === "duplicate" ? 1 : 0,
    loadModel: config.mode === "load"
      ? "사용자별 추가/제거 교대, ACK와 독립적인 일정 주기 전송. 락 대기는 별도 DB 수집 결과와 대조"
      : "같은 회원의 두 연결에서 동일 리액션 동시 추가",
    reactionTargetPerSecond: config.mode === "load" ? config.users * 1000 / config.intervalMs : null,
    variantIsLabelOnly: true };
}

export default function (seed, run) {
  const config = readReactionConfig(__ENV);
  const target = run.plan.targets[scenario.iterationInTest];
  if (!target) throw new Error("현재 VU에 배정할 리액션 사용자가 없습니다");
  return runReactionUser(target, config, run);
}
