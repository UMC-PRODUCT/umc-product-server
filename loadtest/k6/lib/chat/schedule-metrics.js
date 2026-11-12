import { Counter, Gauge, Trend } from "k6/metrics";

const planned = new Counter("umc_chat_scheduled");
const issued = new Counter("umc_chat_issued");
const dropped = new Counter("umc_chat_schedule_dropped");
const lag = new Trend("umc_chat_schedule_lag_ms", true);
const pending = new Gauge("umc_chat_pending_acks");
const pendingAtEnd = new Counter("umc_chat_pending_at_measurement_end");

export function recordSchedule(sample, tags) {
  if (sample.planned !== undefined) planned.add(sample.planned, tags);
  if (sample.issued !== undefined) issued.add(sample.issued, tags);
  if (sample.dropped !== undefined) dropped.add(sample.dropped, tags);
  if (sample.lag !== undefined) lag.add(sample.lag, tags);
  // user_slot은 실행 시 고정된 사용자 번호이며 DB 회원 ID를 노출하지 않는다.
  if (sample.pending !== undefined) pending.add(sample.pending, tags);
  if (sample.pendingAtEnd !== undefined) pendingAtEnd.add(sample.pendingAtEnd, tags);
}
