import { Counter, Rate, Trend } from "k6/metrics";

const attempts = new Counter("umc_chat_reaction_attempts");
const successes = new Counter("umc_chat_reaction_successes");
const errors = new Rate("umc_chat_reaction_error_rate");
const failures = new Counter("umc_chat_reaction_failures");
const latency = new Trend("umc_chat_reaction_ack_latency_ms", true);
const warmup = new Counter("umc_chat_reaction_warmup_attempts");
const ready = new Counter("umc_chat_reaction_ready_users");

export function initializeReactionMetrics(tags) {
  for (const counter of [attempts, successes, failures, warmup, ready]) counter.add(0, tags);
}

export function reactionAttempt(tags, measured) {
  (measured ? attempts : warmup).add(1, tags);
}

export function reactionResult(tags, elapsedMs, success) {
  errors.add(!success, tags);
  if (success) { successes.add(1, tags); latency.add(elapsedMs, tags); }
}

export function reactionFailure(tags, error) {
  const known = ["timeout", "rejected", "mismatch", "send_error", "stomp_error", "frame_error",
    "connection_error", "connection_closed", "unexpected"];
  failures.add(1, { ...tags, reason: known.includes(error.reason) ? error.reason : "unexpected" });
}

export function reactionReady(tags) { ready.add(1, tags); }

export function reactionSummary(summary, config) {
  const count = (name) => summary?.metrics?.[name]?.values?.count || 0;
  const attempts = count("umc_chat_reaction_attempts");
  const successes = count("umc_chat_reaction_successes");
  // k6 기본 Counter rate의 분모에는 연결 준비와 워밍업도 들어가므로 본 측정 구간으로 별도 계산한다.
  return { attempts, successes, failures: count("umc_chat_reaction_failures"),
    measurementSeconds: config.mode === "load" ? config.durationMs / 1000 : null,
    attemptedPerSecond: config.mode === "load" ? attempts / (config.durationMs / 1000) : null,
    successfulPerSecond: config.mode === "load" ? successes / (config.durationMs / 1000) : null };
}
