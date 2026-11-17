import http from "k6/http";
import clock from "k6/x/monotonic";
import { check } from "k6";
import { Counter, Gauge, Rate, Trend } from "k6/metrics";
import { getAccessToken } from "../auth.js";
import { ChatClient } from "./chat-client.js";
import { StompError } from "../stomp/stomp.js";

const attempted = new Counter("umc_chat_lock_attempts");
const succeeded = new Counter("umc_chat_lock_successes");
const failed = new Counter("umc_chat_lock_failures");
const errorRate = new Rate("umc_chat_lock_error_rate");
const latency = new Trend("umc_chat_lock_latency_ms", true);
const noops = new Counter("umc_chat_lock_noops");
const invalidTiming = new Counter("umc_chat_lock_invalid_timing");
export const lockRunFailures = new Counter("umc_chat_lock_run_failures");
export const lockMeasurementStart = new Gauge("umc_chat_lock_measurement_start_ms");
export const lockMeasurementEnd = new Gauge("umc_chat_lock_measurement_end_ms");
export const pause = (ms) => new Promise((resolve) => setTimeout(resolve, Math.max(0, ms)));

export function requireLockCheck(ok, name) {
  check(ok, { [name]: (value) => value });
  if (!ok) throw new StompError(name, "mismatch");
}

export class LockClient extends ChatClient {
  constructor(memberId, config) {
    super(memberId, config);
    this.token = getAccessToken(memberId, { baseUrl: config.baseUrl, timeout: config.timeoutMs + "ms" });
  }

  async request(method, path, body, { timeoutMs = this.config.timeoutMs, tags = this.config.tags } = {}) {
    const response = await http.asyncRequest(method, this.config.baseUrl + path,
      body === undefined ? null : JSON.stringify(body), {
        headers: { Authorization: "Bearer " + this.token, "Content-Type": "application/json" },
        timeout: timeoutMs + "ms", redirects: 0, tags,
      });
    if (response.status < 200 || response.status >= 300) {
      throw new StompError("HTTP 요청 실패: " + response.status + " " + method, "http_" + response.status);
    }
    const envelope = response.json();
    if (envelope?.success !== true || !envelope.result) throw new StompError("HTTP 응답 계약 오류", "mismatch");
    return envelope.result;
  }

  async prepareHttpConnection(threadId, startAt, phase) {
    const prepareAt = startAt - 5000;
    do { await pause(prepareAt - clock.now()); } while (clock.now() < prepareAt);
    const requestedAt = clock.now();
    requireLockCheck(requestedAt < startAt, "HTTP 준비 확인을 공통 시작 전에 요청");
    const detail = await this.request("GET", "/api/v1/community/threads/" + threadId, undefined, {
      timeoutMs: Math.min(this.config.timeoutMs, startAt - requestedAt),
      tags: { ...this.config.tags, scenario: "connection-prepare", phase },
    });
    requireLockCheck(detail.isJoined === true, "HTTP 준비 확인에서 방 참여 상태 확인");
    const completedAt = clock.now();
    requireLockCheck(completedAt < startAt, "HTTP 준비 확인을 공통 시작 전에 완료");
    return { requestedAt, completedAt, plannedStartAt: startAt };
  }

  async command(target, command, body) {
    const commandId = crypto.randomUUID();
    const entry = this.track("ack", commandId, { measured: false });
    const pending = this.stomp.waitFor((frame) => {
      if (frame.command !== "MESSAGE") return false;
      const event = frame.event || JSON.parse(frame.body);
      return event.payload?.commandId === commandId || event.commandId === commandId;
    }, command, this.config.timeoutMs, commandId);
    try {
      try { this.stomp.send(target.path, body, { "x-command-id": commandId }); }
      catch (error) {
        this.stomp.cancelWaiters(commandId, error);
        await pending.catch((failure) => { if (failure !== error) throw failure; });
        throw error;
      }
      const frame = await pending;
      const event = frame.event || JSON.parse(frame.body);
      if (event.type !== "command.acknowledged") {
        throw new StompError("STOMP 요청 거절: " + event.code, "rejected");
      }
      if (event.threadId !== target.threadId || event.payload?.messageId !== target.messageId
          || event.payload?.command !== command || typeof event.payload?.deduplicated !== "boolean") {
        throw new StompError("STOMP ACK 식별 정보 오류", "mismatch");
      }
      return event.payload;
    } finally { entry.finished = true; }
  }

  async measure(operation, phase, action) {
    const tags = { ...this.config.tags, operation };
    if (phase !== "measurement") return action();
    const start = clock.now();
    attempted.add(1, tags);
    try {
      const result = await action();
      const elapsed = clock.now() - start;
      if (elapsed < 0) { invalidTiming.add(1, tags); throw new StompError("음수 지연", "invalid_timing"); }
      if (result?.deduplicated === true) {
        noops.add(1, tags);
        throw new StompError("실제 상태 변경이 없는 요청", "noop");
      }
      latency.add(elapsed, tags);
      succeeded.add(1, tags);
      errorRate.add(false, tags);
      return result;
    } catch (error) {
      failed.add(1, { ...tags, reason: error instanceof StompError ? error.reason : "unexpected" });
      errorRate.add(true, tags);
      throw error;
    }
  }

  initializeMetrics() {
    for (const metric of [failed, noops, invalidTiming, lockRunFailures]) metric.add(0, this.config.tags);
  }
}
