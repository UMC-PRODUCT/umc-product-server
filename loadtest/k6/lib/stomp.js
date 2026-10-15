import { WebSocket } from "k6/websockets";

import { encodeFrame, StompDecoder } from "./stomp-protocol.js";

export class StompTimeoutError extends Error {
  constructor(message) {
    super(message);
    this.name = "StompTimeoutError";
  }
}

export class StompClient {
  constructor(url, { token, label, timeoutMs = 10000 }) {
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
  }

  async connect() {
    const host = /^wss?:\/\/([^/]+)/.exec(this.url)?.[1];
    if (!host || this.socket) throw new Error("잘못된 STOMP 연결 주소 또는 중복 연결");
    // WebSocket open은 전송 경로만 열린 상태다. STOMP CONNECTED까지 받아야 인증이 완료된다.
    const ready = this.waitFor((frame) => frame.command === "CONNECTED", "STOMP 인증");
    try {
      this.socket = new WebSocket(this.url, [], { tags: { client: this.label } });
      this.socket.onopen = () => {
        try {
          this.write("CONNECT", {
            "accept-version": "1.2", host, Authorization: `Bearer ${this.token}`,
            "heart-beat": "10000,10000",
          });
        } catch (_) {
          this.fail(new Error(`${this.label}: CONNECT 전송 실패`));
        }
      };
      this.socket.onmessage = (event) => {
        this.lastActivity = Date.now();
        try {
          if (typeof event.data !== "string") throw new Error("텍스트 STOMP 프레임이 아닙니다");
          for (const frame of this.decoder.push(event.data)) {
            if (frame.command === "ERROR") {
              this.fail(new Error(`${this.label}: STOMP ERROR 수신`));
              return;
            }
            this.dispatch(frame);
          }
        } catch (_) {
          // 서버 본문에는 인증 정보가 섞일 수 있어 원문 대신 처리 단계만 오류로 남긴다.
          this.fail(new Error(`${this.label}: STOMP 수신 프레임 처리 실패`));
        }
      };
      this.socket.onerror = () => this.fail(new Error(`${this.label}: WebSocket 연결 오류`));
      this.socket.onclose = () => {
        this.stopHeartbeats();
        if (this.closeWaiter) this.closeWaiter();
        if (!this.closing) this.fail(new Error(`${this.label}: 예상하지 못한 연결 종료`));
      };
    } catch (_) {
      this.fail(new Error(`${this.label}: WebSocket 생성 실패`));
    }
    const frame = await ready;
    this.connected = true;
    this.token = null;
    this.startHeartbeats(frame.headers["heart-beat"]);
    return this;
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
    this.socket.send(encodeFrame(command, headers, body));
  }

  requireConnected() {
    if (this.failure) throw this.failure;
    if (!this.connected || this.closing) throw new Error(`${this.label}: STOMP 연결이 준비되지 않았습니다`);
  }

  waitFor(matches, description, timeoutMs = this.timeoutMs) {
    if (this.failure) return Promise.reject(this.failure);
    if (this.closing) return Promise.reject(new Error(`${this.label}: 종료 중인 연결`));
    // ACK와 상태 이벤트의 도착 순서는 고정되지 않는다. 먼저 도착한 프레임도 버리지 않고 찾는다.
    const index = this.inbox.findIndex(matches);
    if (index >= 0) return Promise.resolve(this.inbox.splice(index, 1)[0]);
    return new Promise((resolve, reject) => {
      const waiter = { matches, resolve, reject, timer: null };
      waiter.timer = setTimeout(() => {
        this.waiters = this.waiters.filter((entry) => entry !== waiter);
        reject(new StompTimeoutError(`${this.label}: ${description} 시간 초과 (${timeoutMs}ms)`));
      }, timeoutMs);
      this.waiters.push(waiter);
    });
  }

  dispatch(frame) {
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
      throw new Error(`${this.label}: 잘못된 heartbeat 응답`);
    }
    const [serverSends, serverReceives] = intervals;
    if (serverReceives > 0) {
      this.timers.push(setInterval(() => {
        try { this.socket.send("\n"); }
        catch (_) { this.fail(new Error(`${this.label}: heartbeat 전송 실패`)); }
      }, Math.max(10000, serverReceives)));
    }
    if (serverSends > 0) {
      const interval = Math.max(10000, serverSends);
      this.timers.push(setInterval(() => {
        if (Date.now() - this.lastActivity > interval * 2.5) {
          this.fail(new Error(`${this.label}: heartbeat 수신 시간 초과`));
        }
      }, interval));
    }
  }

  stopHeartbeats() {
    this.timers.forEach(clearInterval);
    this.timers = [];
  }

  rejectWaiters(error) {
    for (const waiter of this.waiters) {
      clearTimeout(waiter.timer);
      waiter.reject(error);
    }
    this.waiters = [];
  }

  fail(error) {
    if (this.failure) return;
    this.failure = error;
    this.stopHeartbeats();
    this.rejectWaiters(error);
    try { this.socket?.close(); }
    catch (_) { /* 연결 생성 전 실패한 경우에도 원래 오류를 유지한다. */ }
  }

  async close() {
    this.closing = true;
    this.stopHeartbeats();
    this.rejectWaiters(new Error(`${this.label}: 연결 종료`));
    if (!this.socket || this.socket.readyState === WebSocket.CLOSED) return;
    await new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        this.closeWaiter = null;
        reject(new Error(`${this.label}: 연결 종료 시간 초과`));
      }, 2000);
      this.closeWaiter = () => {
        clearTimeout(timer);
        this.closeWaiter = null;
        resolve();
      };
      try {
        // SIMPLE broker에서는 DISCONNECT의 RECEIPT도 기다리지 않고 실제 소켓 종료를 확인한다.
        if (this.socket.readyState === WebSocket.OPEN) this.write("DISCONNECT", {});
        this.socket.close(1000);
      } catch (_) {
        clearTimeout(timer);
        this.closeWaiter = null;
        reject(new Error(`${this.label}: 연결 종료 실패`));
      }
    });
  }
}
