import { WebSocket } from "k6/websockets";

import { encodeFrame, StompDecoder } from "./stomp-protocol.js";

function diagnosticText(value) {
  return String(value ?? "")
    .replace(/Bearer\s+[^\s"']+/gi, "Bearer <redacted>")
    .replace(/[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]+/g, "<redacted>")
    .slice(0, 2048);
}

// 통신 실패 원인을 reason으로 전달한다. 호출부는 오류 원문 대신 이 값을 사용해 실패 지표를 분류한다.
export class StompError extends Error {
  constructor(message, reason) {
    super(message);
    this.name = "StompError";
    this.reason = reason;
  }
}

// 응답·heartbeat·종료 대기의 시간 초과다. Smoke의 구독 준비 단계에서는 이 타입만 재시도 대상으로 삼는다.
export class StompTimeoutError extends StompError {
  constructor(message) {
    super(message, "timeout");
    this.name = "StompTimeoutError";
  }
}

/**
 * WebSocket 연결 하나에서 STOMP 인증·구독·프레임 송수신·heartbeat·응답 대기를 처리한다.
 * Chat의 방이나 메시지 형식은 알지 못한다. Chat 시나리오는 보통 이 클래스를 감싼 ChatClient를 사용한다.
 * 직접 사용할 때는 connect() 후 subscribe/send를 호출하고, finally에서 close()로 타이머와 대기를 정리한다.
 * 특정 응답은 waitFor로 기다리고, 연속 이벤트는 observe로 받는다. 대기는 전송 전에 등록해야 응답을 놓치지 않는다.
 */
export class StompClient {
  constructor(url, { token, label, timeoutMs = 10000, diagnostics }) {
    this.url = url;
    this.token = token;
    this.label = label;
    this.timeoutMs = timeoutMs;
    this.socket = null;
    this.connected = false;
    this.closing = false;
    this.failure = null;
    this.decoder = new StompDecoder();
    this.inbox = [];
    this.waiters = [];
    this.subscriptionSequence = 0;
    this.timers = [];
    this.closeWaiter = null;
    this.lastActivity = Date.now();
    this.observers = new Set();
    this.diagnostics = /^[A-Za-z0-9_-]{1,128}$/.test(diagnostics?.testId ?? "")
      ? { ...diagnostics, connectionId: crypto.randomUUID() } : null;
    this.startedAt = this.lastActivity;
    this.openedAt = null;
    this.lastSentAt = null;
    this.lastReceivedAt = null;
    this.closeRequestedAt = null;
  }

  async connect() {
    const host = /^wss?:\/\/([^/]+)/.exec(this.url)?.[1];
    if (!host || this.socket) throw new Error("잘못된 STOMP 연결 주소 또는 중복 연결");
    // WebSocket open은 전송 경로만 열린 상태다. STOMP CONNECTED까지 받아야 인증이 완료된다.
    const ready = this.waitFor((frame) => frame.command === "CONNECTED", "STOMP 인증");
    try {
      this.logConnection("connecting");
      this.socket = new WebSocket(this.url, [], { tags: { client: this.label },
        ...(this.diagnostics ? { headers: {
          "X-Chat-Test-Id": this.diagnostics.testId,
          "X-Chat-Connection-Id": this.diagnostics.connectionId,
        } } : {}) });
      this.socket.onopen = () => {
        this.openedAt = Date.now();
        this.logConnection("opened");
        try {
          this.write("CONNECT", {
            "accept-version": "1.2", host, Authorization: `Bearer ${this.token}`,
            "heart-beat": "10000,10000",
          });
        } catch (_) {
          this.fail(new StompError(`${this.label}: CONNECT 전송 실패`, "send_error"));
        }
      };
      this.socket.onmessage = (event) => {
        const receivedAt = Date.now();
        this.lastActivity = receivedAt;
        this.lastReceivedAt = receivedAt;
        try {
          if (typeof event.data !== "string") throw new Error("텍스트 STOMP 프레임이 아닙니다");
          for (const frame of this.decoder.push(event.data)) {
            // 두 응답을 모두 기다린 시각이 아니라 각 프레임을 받은 시각으로 지연을 계산한다.
            frame.receivedAt = receivedAt;
            if (frame.command === "ERROR") {
              this.fail(new StompError(`${this.label}: STOMP ERROR 수신`, "stomp_error"));
              return;
            }
            this.dispatch(frame);
          }
        } catch (_) {
          // 서버 본문에는 인증 정보가 섞일 수 있어 원문 대신 처리 단계만 오류로 남긴다.
          this.fail(new StompError(`${this.label}: STOMP 수신 프레임 처리 실패`, "frame_error"));
        }
      };
      this.socket.onerror = (event) => {
        this.logConnection("transport_error", {
          error: diagnosticText(event.error?.message ?? event.error ?? event.message),
        });
        // DISCONNECT 이후 서버가 먼저 닫는 경우도 있다. 실제 종료 여부는 close()가 확인한다.
        if (!this.closing) this.fail(new StompError(`${this.label}: WebSocket 연결 오류`, "connection_error"));
      };
      this.socket.onclose = (event) => {
        this.logConnection("closed", { code: event.code ?? null, reason: diagnosticText(event.reason),
          wasClean: event.wasClean ?? null, expected: this.closing });
        this.stopHeartbeats();
        if (this.closeWaiter) this.closeWaiter();
        if (!this.closing) this.fail(new StompError(`${this.label}: 예상하지 못한 연결 종료`, "connection_closed"));
        this.observers.clear();
      };
    } catch (error) {
      this.logConnection("creation_error", { error: diagnosticText(error instanceof Error ? error.message : error) });
      this.fail(new StompError(`${this.label}: WebSocket 생성 실패`, "connection_error"));
    }
    const frame = await ready;
    this.connected = true;
    this.token = null;
    this.startHeartbeats(frame.headers["heart-beat"]);
    this.logConnection("stomp_connected");
    return this;
  }

  logConnection(event, details = {}) {
    if (!this.diagnostics) return;
    console.log("[DEBUG-ws-connection] " + JSON.stringify({ event, at: new Date().toISOString(),
      ...this.diagnostics, label: this.label, startedAt: this.startedAt, openedAt: this.openedAt,
      lastSentAt: this.lastSentAt, lastReceivedAt: this.lastReceivedAt, closeRequestedAt: this.closeRequestedAt,
      readyState: this.socket?.readyState ?? null, failureReason: this.failure?.reason ?? null, ...details }));
  }

  subscribe(destination) {
    this.requireConnected();
    const id = `subscription-${++this.subscriptionSequence}`;
    this.write("SUBSCRIBE", { id, destination, ack: "auto" });
    return id;
  }

  send(destination, payload, headers = {}) {
    this.requireConnected();
    this.write("SEND", { ...headers, destination, "content-type": "application/json" }, JSON.stringify(payload));
  }

  write(command, headers, body = "") {
    try {
      this.socket.send(encodeFrame(command, headers, body));
    } catch (error) {
      this.logConnection("send_error", { command,
        error: diagnosticText(error instanceof Error ? error.message : error) });
      throw error;
    }
    this.lastSentAt = Date.now();
  }

  requireConnected() {
    if (this.failure) throw this.failure;
    if (!this.connected || this.closing) throw new Error(`${this.label}: STOMP 연결이 준비되지 않았습니다`);
  }

  // key를 요청별로 지정하면 cancelWaiters(key, error)로 해당 요청의 대기만 취소할 수 있다.
  waitFor(matches, description, timeoutMs = this.timeoutMs, key) {
    if (this.failure) return Promise.reject(this.failure);
    if (this.closing) return Promise.reject(new Error(`${this.label}: 종료 중인 연결`));
    // ACK와 상태 이벤트의 도착 순서는 고정되지 않는다. 먼저 도착한 프레임도 버리지 않고 찾는다.
    const index = this.inbox.findIndex(matches);
    if (index >= 0) return Promise.resolve(this.inbox.splice(index, 1)[0]);
    return new Promise((resolve, reject) => {
      const waiter = { matches, resolve, reject, key, timer: null };
      waiter.timer = setTimeout(() => {
        this.waiters = this.waiters.filter((entry) => entry !== waiter);
        reject(new StompTimeoutError(`${this.label}: ${description} 시간 초과 (${timeoutMs}ms)`));
      }, timeoutMs);
      this.waiters.push(waiter);
    });
  }

  // listener(frame)가 true를 반환하면 해당 프레임은 소비된 것으로 처리한다. 반환된 함수로 관측자를 해제한다.
  observe(listener) {
    this.observers.add(listener);
    return () => this.observers.delete(listener);
  }

  dispatch(frame) {
    // 관측자가 true를 반환한 중복 프레임은 대기자나 미처리 버퍼에 다시 전달하지 않는다.
    let consumed = false;
    for (const observer of this.observers) consumed = observer(frame) || consumed;
    if (consumed) return;
    const index = this.waiters.findIndex((waiter) => waiter.matches(frame));
    if (index >= 0) {
      const [waiter] = this.waiters.splice(index, 1);
      clearTimeout(waiter.timer);
      waiter.resolve(frame);
    } else {
      if (this.inbox.length >= 512) throw new Error("STOMP 미처리 프레임 제한 초과");
      this.inbox.push(frame);
    }
  }

  startHeartbeats(header = "0,0") {
    const intervals = header.split(",").map(Number);
    if (intervals.length !== 2 || intervals.some((value) => !Number.isInteger(value) || value < 0)) {
      throw new StompError(`${this.label}: 잘못된 heartbeat 응답`, "frame_error");
    }
    const [serverSends, serverReceives] = intervals;
    if (serverReceives > 0) {
      this.timers.push(setInterval(() => {
        try { this.socket.send("\n"); this.lastSentAt = Date.now(); }
        catch (error) {
          this.logConnection("heartbeat_send_error", {
            error: diagnosticText(error instanceof Error ? error.message : error),
          });
          this.fail(new StompError(`${this.label}: heartbeat 전송 실패`, "send_error"));
        }
      }, Math.max(10000, serverReceives)));
    }
    if (serverSends > 0) {
      const interval = Math.max(10000, serverSends);
      this.timers.push(setInterval(() => {
        if (Date.now() - this.lastActivity > interval * 2.5) {
          this.fail(new StompTimeoutError(`${this.label}: heartbeat 수신 시간 초과`));
        }
      }, interval));
    }
  }

  stopHeartbeats() {
    this.timers.forEach(clearInterval);
    this.timers = [];
  }

  rejectWaiters(error) {
    this.cancelWaiters(undefined, error);
  }

  cancelWaiters(key, error) {
    const cancelled = this.waiters.filter((waiter) => key === undefined || waiter.key === key);
    this.waiters = this.waiters.filter((waiter) => !cancelled.includes(waiter));
    for (const waiter of cancelled) {
      clearTimeout(waiter.timer);
      waiter.reject(error);
    }
  }

  fail(error) {
    if (this.failure) return;
    this.failure = error;
    this.logConnection("failure", { reason: error.reason ?? null, error: diagnosticText(error.message) });
    this.stopHeartbeats();
    this.rejectWaiters(error);
    try {
      this.closeRequestedAt = Date.now();
      this.logConnection("close_requested", { cause: "failure" });
      this.socket?.close();
    } catch (closeError) {
      this.logConnection("close_error", {
        error: diagnosticText(closeError instanceof Error ? closeError.message : closeError),
      });
    }
  }

  async close() {
    this.closing = true;
    this.stopHeartbeats();
    this.rejectWaiters(new Error(`${this.label}: 연결 종료`));
    if (!this.socket || this.socket.readyState === WebSocket.CLOSED) {
      this.observers.clear();
      return;
    }
    await new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        this.logConnection("close_timeout");
        this.closeWaiter = null;
        this.observers.clear();
        reject(new StompTimeoutError(`${this.label}: 연결 종료 시간 초과`));
      }, 2000);
      this.closeWaiter = () => {
        clearTimeout(timer);
        this.closeWaiter = null;
        resolve();
      };
      try {
        // SIMPLE broker에서는 DISCONNECT의 RECEIPT도 기다리지 않고 실제 소켓 종료를 확인한다.
        if (this.socket.readyState === WebSocket.OPEN) this.write("DISCONNECT", {});
        this.closeRequestedAt = Date.now();
        this.logConnection("close_requested", { cause: "cleanup", code: 1000 });
        this.socket.close(1000);
      } catch (error) {
        this.logConnection("close_error", {
          error: diagnosticText(error instanceof Error ? error.message : error),
        });
        clearTimeout(timer);
        this.closeWaiter = null;
        this.observers.clear();
        reject(new StompError(`${this.label}: 연결 종료 실패`, "connection_error"));
      }
    });
  }
}
