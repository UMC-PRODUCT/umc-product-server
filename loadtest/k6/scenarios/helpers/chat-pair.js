import { ChatClient } from "../../lib/chat/chat-client.js";
import { StompError, StompTimeoutError } from "../../lib/stomp/stomp.js";
import { recordFailure, recordMessageResult } from "../../lib/chat/chat-metrics.js";

export function selectPair(seed, config) {
  if (seed?.baseUrl?.replace(/\/+$/, "") !== config.baseUrl) {
    throw new Error("BASE_URL과 시드의 baseUrl이 다릅니다. 대상 앱에서 데이터를 다시 준비하세요");
  }
  if (!Array.isArray(seed.rooms) || seed.rooms.length === 0) {
    throw new Error("Smoke에 사용할 두 사용자·한 방 시드가 부족합니다");
  }
  // Smoke는 첫 번째 방의 두 사용자만 사용한다. 나머지 방의 구성은 검사하지 않는다.
  const room = seed.rooms[0];
  const validId = (id) => typeof id === "string" && /^[1-9][0-9]*$/.test(id);
  if (!validId(room?.threadId) || !Array.isArray(room.memberIds) || room.memberIds.length !== 2
      || !room.memberIds.every(validId) || !Array.isArray(seed.memberIds)
      || !room.memberIds.every((id) => seed.memberIds.includes(id))) {
    throw new Error("Smoke의 첫 번째 방에 유효한 회원 ID 두 개가 필요합니다");
  }
  if (room.memberIds[0] === room.memberIds[1]) {
    throw new Error("Smoke의 첫 번째 방에는 서로 다른 두 회원이 필요합니다");
  }
  return { threadId: room.threadId, senderId: room.memberIds[0], receiverId: room.memberIds[1] };
}

/**
 * Smoke의 발신자·수신자 두 명을 함께 관리하고, ACK와 상대방 수신이 같은 메시지를 가리키는지 확인한다.
 * target은 selectPair()로 선택한다. 시나리오에서는 직접 수명 주기를 관리하기보다 runChatPair()를 사용한다.
 * open()에서 측정에 포함하지 않는 준비 메시지로 구독을 확인하고, sendMessage()에서 한 건을 측정한다.
 * connected·ready·lastResult·closed는 Smoke의 최종 check에 사용한다. 반복 부하의 사용자 배정은 담당하지 않는다.
 */
export class ChatPairSession {
  constructor(target, config) {
    this.target = target;
    this.config = config;
    this.clients = [];
    this.connected = false;
    this.ready = false;
    this.closed = false;
    this.failure = null;
    this.lastResult = {};
  }

  async open() {
    let stage = "auth";
    try {
      this.clients = [
        new ChatClient(this.target.senderId, this.config, { label: "sender" }),
        new ChatClient(this.target.receiverId, this.config, { label: "receiver" }),
      ];
      await Promise.all(this.clients.map((client) => client.open()));
      this.connected = true;
      stage = "subscription";
      // SIMPLE broker의 구독 RECEIPT 대신 실제 송수신으로 준비를 확인한다. 본 측정에는 포함하지 않는다.
      for (let attempt = 1; attempt <= 3; attempt++) {
        try {
          await this.sendMessage({ measured: false, phase: "준비", timeoutMs: Math.min(this.config.timeoutMs, 3000) });
          this.ready = true;
          return;
        } catch (error) {
          if (!(error instanceof StompTimeoutError) || attempt === 3) throw error;
        }
      }
    } catch (error) {
      this.failure = error;
      recordFailure(stage === "subscription" ? stage : (error.stage || stage), error, this.config.tags);
      throw error;
    }
  }

  async sendMessage({ measured = true, phase = "부하", timeoutMs = this.config.timeoutMs } = {}) {
    const [sender, receiver] = this.clients;
    const message = sender.createMessage(this.target.threadId, { measured, phase });
    const result = this.lastResult = { acknowledged: false, delivered: false, correlated: false };
    let success = false;
    const cancelOther = (peer) => (error) => {
      peer.cancelMessage(message);
      throw error;
    };
    // 수신 대기를 먼저 등록해야 전송 직후 도착하는 이벤트도 놓치지 않는다.
    const delivered = receiver.waitForMessage(message, { timeoutMs }).then((value) => {
      result.delivered = true;
      return value;
    }).catch(cancelOther(sender));
    const acknowledged = sender.sendMessage(message, { timeoutMs }).then((value) => {
      result.acknowledged = true;
      return value;
    }).catch(cancelOther(receiver));
    try {
      const results = await Promise.allSettled([acknowledged, delivered]);
      const errors = results.filter((entry) => entry.status === "rejected" && entry.reason.reason !== "cancelled");
      for (const error of errors) {
        if (measured) recordFailure(error.reason.stage, error.reason, this.config.tags);
      }
      if (errors.length) throw errors[0].reason;
      if (results[0].value.messageId !== results[1].value.messageId) {
        const error = new StompError("ACK와 수신 이벤트가 서로 다른 메시지를 가리킵니다", "mismatch");
        if (measured) recordFailure("correlation", error, this.config.tags);
        throw error;
      }
      result.correlated = success = true;
      return results[1].value;
    } catch (error) {
      if (measured) this.failure = error;
      throw error;
    } finally {
      // 개별 ACK 성공과 두 사용자 간 송수신 성공은 측정 경계가 다르므로 여기서 최종 결과를 센다.
      if (measured) recordMessageResult(success, this.config.tags);
    }
  }

  async close() {
    const results = await Promise.allSettled(this.clients.map((client) => client.close()));
    this.closed = results.every((entry) => entry.status === "fulfilled");
    const connectionFailure = this.clients.find((client) => client.failure)?.failure;
    const closeFailure = results.find((entry) => entry.status === "rejected")?.reason;
    if (!this.failure && connectionFailure) {
      this.failure = connectionFailure;
      recordFailure("connection", connectionFailure, this.config.tags);
    }
    if (closeFailure) {
      this.failure ||= closeFailure;
      recordFailure("cleanup", closeFailure, this.config.tags);
    }
    if (this.failure) throw this.failure;
  }
}

// action(session)에 Smoke의 검증 행동을 넘긴다. 준비나 행동이 실패해도 연결을 닫고 { session, failure }를 반환한다.
export async function runChatPair(target, config, action) {
  const session = new ChatPairSession(target, config);
  let failure = null;
  try {
    await session.open();
    await action(session);
  } catch (error) {
    failure = error;
  } finally {
    try { await session.close(); }
    catch (error) { failure ||= error; }
  }
  return { session, failure };
}
