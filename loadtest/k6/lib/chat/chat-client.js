import { getAccessToken } from "../auth.js";
import { StompClient, StompError, StompTimeoutError } from "../stomp/stomp.js";
import { recordAttempt, recordDuplicate, recordLatency } from "./chat-metrics.js";

const EVENTS = "/user/queue/community/threads/events";
const ERRORS = "/user/queue/errors";
const HISTORY_LIMIT = 512;

function eventOf(frame) {
  if (frame.command !== "MESSAGE") return null;
  if (!frame.event) frame.event = JSON.parse(frame.body);
  return frame.event;
}

function atStage(error, stage) {
  if (error.stage) return error;
  const failure = error instanceof StompTimeoutError ? new StompTimeoutError(error.message)
    : new StompError(error.message, error.reason || "unexpected");
  failure.stage = stage;
  return failure;
}

/**
 * 테스트 회원 한 명의 인증·STOMP 연결·Chat 이벤트 처리를 담당한다. 인스턴스 하나가 연결 하나를 가진다.
 * 사용자 수나 방 배정, 반복 전송 여부는 시나리오에서 정하므로 Smoke와 반복 부하에서 함께 사용한다.
 * memberId와 연결 설정으로 생성하고 open() → 송수신 → finally에서 close() 순서로 사용한다.
 * open()은 구독 요청까지만 보내므로, 실제 수신 준비 여부는 시나리오에서 별도로 확인해야 한다.
 */
export class ChatClient {
  constructor(memberId, config, { label = "chat-user" } = {}) {
    if (typeof memberId !== "string" || !/^[1-9][0-9]*$/.test(memberId)) {
      throw new Error("유효한 회원 ID가 필요합니다");
    }
    this.memberId = memberId;
    this.config = config;
    this.label = label;
    this.stomp = null;
    this.history = new Map();
    this.listeners = new Set();
  }

  async open() {
    if (this.stomp) throw new Error("ChatClient는 한 번만 연결할 수 있습니다");
    let stage = "auth";
    try {
      const token = getAccessToken(this.memberId,
        { baseUrl: this.config.baseUrl, timeout: `${this.config.timeoutMs}ms` });
      this.stomp = new StompClient(`${this.config.baseUrl.replace(/^http/, "ws")}/ws/websocket`,
        { token, label: this.label, timeoutMs: this.config.timeoutMs });
      stage = "connect";
      await this.stomp.connect();
      this.stomp.observe((frame) => this.route(frame));
      stage = "subscription";
      this.stomp.subscribe(EVENTS);
      this.stomp.subscribe(ERRORS);
      // 구독 프레임 전송까지 수행한다. 실제 구독 준비 확인 방법은 시나리오에서 선택한다.
      return this;
    } catch (error) {
      throw atStage(error, stage);
    }
  }

  get failure() {
    return this.stomp?.failure;
  }

  // 계속 들어오는 이벤트는 listener(event, receivedAt)로 받는다. 반환된 함수를 호출하면 구독한 콜백을 해제한다.
  onEvent(listener) {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  route(frame) {
    const event = eventOf(frame);
    if (!event) return false;
    const key = event.type === "command.acknowledged" ? `ack:${event.payload?.commandId}`
      : event.type === "message.created" ? `delivery:${event.payload?.clientMessageId}`
        : `ack:${event.commandId}`;
    const entry = this.history.get(key);
    if (entry?.seen) {
      if (entry.message.measured) recordDuplicate(entry.kind, this.config.tags);
      return true;
    }
    if (entry) entry.seen = true;
    for (const listener of this.listeners) listener(event, frame.receivedAt);
    // 기다리는 대상이 아닌 이벤트는 콜백으로 전달하고 버퍼에 남기지 않는다.
    return !entry || entry.finished;
  }

  track(kind, id, message) {
    const key = `${kind}:${id}`;
    if (this.history.has(key)) throw new Error("같은 응답을 중복 대기할 수 없습니다");
    if (this.history.size >= HISTORY_LIMIT) {
      const completed = [...this.history].find(([, entry]) => entry.finished);
      if (!completed) throw new Error("동시에 대기할 수 있는 Chat 요청 수를 초과했습니다");
      this.history.delete(completed[0]);
    }
    // 완료된 기록도 최근 512개까지 보관해 뒤늦게 도착한 중복을 관측한다.
    const entry = { kind, message, seen: false, finished: false };
    this.history.set(key, entry);
    return entry;
  }

  // 전송 전 요청 식별자를 만든다. 같은 VU의 수신자가 이 객체로 먼저 대기한 뒤 송신자가 전송할 수 있다.
  createMessage(threadId, { phase = "부하", content, measured = true } = {}) {
    if (typeof threadId !== "string" || !/^[1-9][0-9]*$/.test(threadId)) {
      throw new Error("유효한 방 ID가 필요합니다");
    }
    const clientMessageId = crypto.randomUUID();
    return { threadId, senderId: this.memberId, commandId: crypto.randomUUID(), clientMessageId,
      content: content ?? `Chat ${phase} 송수신 확인 ${clientMessageId}`, measured, sentAt: null };
  }

  // createMessage()의 결과를 전송하고 새 메시지 생성 ACK를 반환한다. 상대방 수신까지 보장하는 함수는 아니다.
  async sendMessage(message, { timeoutMs = this.config.timeoutMs, contentAtSend } = {}) {
    if (!this.stomp) throw new Error("ChatClient를 먼저 연결하세요");
    if (message.senderId !== this.memberId) throw new Error("다른 회원의 메시지를 전송할 수 없습니다");
    const entry = this.track("ack", message.commandId, message);
    const pending = this.stomp.waitFor((frame) => {
      const event = eventOf(frame);
      return event?.payload?.commandId === message.commandId || event?.commandId === message.commandId;
    }, "ACK", timeoutMs, message.commandId);
    try {
      if (message.measured) recordAttempt(this.config.tags);
      message.sentAt = Date.now();
      try {
        // 다른 VU도 전송 시각을 읽을 수 있도록, 실제 전송 직전에 테스트 본문을 구성할 수 있다.
        if (contentAtSend) message.content = contentAtSend(message.sentAt);
        this.stomp.send(`/app/community/threads/${message.threadId}/messages`,
          { clientMessageId: message.clientMessageId, type: "TEXT", content: message.content },
          { "x-command-id": message.commandId });
      } catch (error) {
        this.cancelMessage(message);
        await pending.catch(() => {});
        const failure = error instanceof StompError ? error : new StompError("메시지 전송 실패", "send_error");
        throw atStage(failure, "send");
      }
      const frame = await pending;
      const event = eventOf(frame);
      if (event.type !== "command.acknowledged") {
        throw new StompError(`요청 거절: status=${event.status}, code=${event.code}`, "rejected");
      }
      const ack = event.payload;
      if (event.threadId !== message.threadId || ack.command !== "MESSAGE_CREATE"
          || ack.clientMessageId !== message.clientMessageId || ack.deduplicated !== false) {
        throw new StompError("ACK의 요청 식별 정보가 다릅니다", "mismatch");
      }
      if (message.measured) recordLatency("ack", frame.receivedAt - message.sentAt, this.config.tags);
      return ack;
    } catch (error) {
      throw atStage(error, "ack");
    } finally {
      entry.finished = true;
    }
  }

  // 같은 VU에서 공유한 메시지 객체의 수신을 기다린다. 다른 VU의 수신 검증에는 onEvent와 DeliveryTracker를 쓴다.
  async waitForMessage(message, { timeoutMs = this.config.timeoutMs } = {}) {
    if (!this.stomp) throw new Error("ChatClient를 먼저 연결하세요");
    const entry = this.track("delivery", message.clientMessageId, message);
    try {
      const frame = await this.stomp.waitFor((candidate) => {
        const event = eventOf(candidate);
        return event?.type === "message.created" && event.payload?.clientMessageId === message.clientMessageId;
      }, "상대방 수신", timeoutMs, message.commandId);
      const event = eventOf(frame);
      const received = event.payload.message;
      if (event.threadId !== message.threadId || received?.senderId !== message.senderId
          || received.threadId !== message.threadId || received.type !== "TEXT"
          || received.clientMessageId !== message.clientMessageId || received.content !== message.content) {
        throw new StompError("수신 메시지의 발신자·본문·식별 정보가 다릅니다", "mismatch");
      }
      // 같은 VU에서 기다릴 때 쓰는 편의 기능이다. 별도 VU 수신 측정은 본문의 전송 시각을 이용한다.
      if (message.measured) {
        if (!Number.isFinite(message.sentAt)) throw new Error("전달 지연 측정에 전송 시각이 필요합니다");
        recordLatency("delivery", frame.receivedAt - message.sentAt, this.config.tags);
      }
      return received;
    } catch (error) {
      throw atStage(error, "delivery");
    } finally {
      entry.finished = true;
    }
  }

  cancelMessage(message) {
    this.stomp?.cancelWaiters(message.commandId, new StompError("해당 요청의 응답 대기 취소", "cancelled"));
  }

  async close() {
    try {
      await this.stomp?.close();
    } finally {
      this.history.clear();
      this.listeners.clear();
    }
  }
}
