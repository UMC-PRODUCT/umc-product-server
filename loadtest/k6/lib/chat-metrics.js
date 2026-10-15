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
const TAGS = { scenario: "chat-message" };

// ID나 오류 원문을 태그로 쓰지 않아 실행할수록 시계열 종류가 늘어나지 않게 한다.
const REASONS = new Set(["timeout", "rejected", "mismatch", "send_error", "stomp_error",
  "frame_error", "connection_error", "connection_closed", "unexpected"]);

export function initializeMetrics() {
  for (const counter of [attempts, successes, failures, stageFailures, duplicates]) counter.add(0, TAGS);
  // Rate에 임의의 성공 표본을 넣으면 실패율의 분모가 바뀐다. 실제 전송 시도 결과만 추가한다.
}

export function recordAttempt() {
  attempts.add(1, TAGS);
}

export function recordMessageResult(success) {
  (success ? successes : failures).add(1, TAGS);
  errorRate.add(!success, TAGS);
}

export function recordLatency(kind, elapsedMs) {
  (kind === "ack" ? ackLatency : deliveryLatency).add(elapsedMs, TAGS);
}

export function recordFailure(stage, error) {
  const reason = REASONS.has(error.reason) ? error.reason : "unexpected";
  stageFailures.add(1, { ...TAGS, stage, reason });
  console.warn(`Chat 실패: stage=${stage}, reason=${reason}`);
}

export function recordDuplicate(kind) {
  duplicates.add(1, { ...TAGS, kind });
}
