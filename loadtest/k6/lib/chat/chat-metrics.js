import { Counter, Rate, Trend } from "k6/metrics";

// 지연 값의 단위는 ms다. 준비 메시지는 제외하고 검증에 통과한 응답만 표본으로 남긴다.
export const ackLatency = new Trend("umc_chat_ack_latency_ms", true);
export const deliveryLatency = new Trend("umc_chat_delivery_latency_ms", true);
const attempts = new Counter("umc_chat_message_attempts");
const successes = new Counter("umc_chat_message_successes");
const failures = new Counter("umc_chat_message_failures");
const errorRate = new Rate("umc_chat_message_error_rate");
const stageFailures = new Counter("umc_chat_failures");
const duplicates = new Counter("umc_chat_duplicate_events");
const ackSuccesses = new Counter("umc_chat_ack_successes");
const ackFailures = new Counter("umc_chat_ack_failures");
const ackErrorRate = new Rate("umc_chat_ack_error_rate");
const deliveryExpected = new Counter("umc_chat_delivery_expected");
const deliveryReceived = new Counter("umc_chat_delivery_received");
const deliveryMissing = new Counter("umc_chat_delivery_missing");
const deliveryLate = new Counter("umc_chat_delivery_late");
const completionMissing = new Counter("umc_chat_completion_missing");
const TAGS = { scenario: "chat-message" };

// ID나 오류 원문을 태그로 쓰지 않아 실행할수록 시계열 종류가 늘어나지 않게 한다.
const REASONS = new Set(["timeout", "rejected", "mismatch", "send_error", "stomp_error",
  "frame_error", "connection_error", "connection_closed", "unexpected"]);

export function initializeMetrics(tags = TAGS) {
  for (const counter of [attempts, successes, failures, stageFailures, duplicates]) counter.add(0, tags);
  // Rate에 임의의 성공 표본을 넣으면 실패율의 분모가 바뀐다. 실제 전송 시도 결과만 추가한다.
}

export function recordAttempt(tags = TAGS) {
  attempts.add(1, tags);
}

export function recordMessageResult(success, tags = TAGS) {
  (success ? successes : failures).add(1, tags);
  errorRate.add(!success, tags);
}

export function recordLatency(kind, elapsedMs, tags = TAGS) {
  (kind === "ack" ? ackLatency : deliveryLatency).add(elapsedMs, tags);
}

export function recordFailure(stage, error, tags = TAGS) {
  const reason = REASONS.has(error.reason) ? error.reason : "unexpected";
  stageFailures.add(1, { ...tags, stage, reason });
  console.warn(`Chat 실패: stage=${stage}, reason=${reason}`);
}

export function recordDuplicate(kind, tags = TAGS) {
  duplicates.add(1, { ...tags, kind });
}

export function initializeLoadMetrics(tags) {
  // Smoke의 메시지 성공은 ACK+상대방 수신이다. 분리된 VU에서는 ACK와 전달 결과를 따로 집계한다.
  for (const counter of [attempts, stageFailures, duplicates, ackSuccesses, ackFailures,
    deliveryExpected, deliveryReceived, deliveryMissing, deliveryLate, completionMissing]) counter.add(0, tags);
}

export function recordAckResult(success, tags) {
  (success ? ackSuccesses : ackFailures).add(1, tags);
  ackErrorRate.add(!success, tags);
}

export function recordDelivery(result, tags) {
  if (result.duplicate) { recordDuplicate("delivery", tags); return; }
  deliveryReceived.add(1, tags);
  recordLatency("delivery", result.latencyMs, tags);
  if (result.late) deliveryLate.add(1, tags);
}

export function recordDeliverySummary(summary, tags) {
  deliveryExpected.add(summary.expected, tags);
  deliveryMissing.add(summary.missing, tags);
  completionMissing.add(summary.completionMissing, tags);
}
