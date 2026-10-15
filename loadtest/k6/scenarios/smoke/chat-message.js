import { check } from "k6";

import { getAccessToken } from "../../lib/auth.js";
import { BASE_URL } from "../../lib/http.js";
import { StompClient, StompTimeoutError } from "../../lib/stomp.js";

export const requiresSeed = true;
const TIMEOUT_MS = Number(__ENV.CHAT_TIMEOUT_MS || 10000);
if (!Number.isSafeInteger(TIMEOUT_MS) || TIMEOUT_MS < 100 || TIMEOUT_MS > 60000) {
  throw new Error("CHAT_TIMEOUT_MS는 100~60000 사이 정수여야 합니다");
}

// 한 VU가 두 회원의 연결을 동시에 관리한다. health-check처럼 일정 시간 반복하지 않고 한 번만 검증한다.
export const options = {
  scenarios: {
    chat_smoke: { executor: "shared-iterations", vus: 1, iterations: 1,
      maxDuration: `${Math.ceil((TIMEOUT_MS * 5 + 5000) / 1000)}s` },
  },
  thresholds: { checks: ["rate==1"], iterations: ["count==1"] },
};

const EVENTS = "/user/queue/community/threads/events";
const ERRORS = "/user/queue/errors";

function validateSeed(seed) {
  const room = seed?.rooms?.[0];
  const ids = room?.memberIds;
  const validId = (id) => typeof id === "string" && /^[1-9][0-9]*$/.test(id);
  if (!validId(room?.threadId) || !Array.isArray(ids) || ids.length !== 2
      || !ids.every(validId) || ids[0] === ids[1]
      || !ids.every((id) => seed.memberIds?.includes(id))) {
    throw new Error("Chat 시드에는 서로 다른 회원 2명과 이들이 참여한 방 하나가 필요합니다");
  }
  if (seed.baseUrl?.replace(/\/+$/, "") !== BASE_URL) {
    throw new Error("BASE_URL과 시드의 baseUrl이 다릅니다. 대상 앱에서 데이터를 다시 준비하세요");
  }
  return { threadId: room.threadId, senderId: ids[0], receiverId: ids[1] };
}

function eventOf(frame) {
  if (frame.command !== "MESSAGE") return null;
  if (!frame.event) frame.event = JSON.parse(frame.body);
  return frame.event;
}

async function exchange(sender, receiver, target, phase, timeoutMs, progress = {}) {
  const commandId = crypto.randomUUID();
  const clientMessageId = crypto.randomUUID();
  const content = `Chat ${phase} 송수신 확인 ${clientMessageId}`;

  // 대기자를 먼저 등록해야 전송 직후 도착하는 ACK·이벤트도 놓치지 않는다.
  const acknowledged = sender.waitFor((frame) => {
    const event = eventOf(frame);
    return event?.payload?.commandId === commandId || event?.commandId === commandId;
  }, `${phase} ACK`, timeoutMs).then((frame) => {
    const event = eventOf(frame);
    if (event.type !== "command.acknowledged") {
      throw new Error(`${phase} 요청 거절: status=${event.status}, code=${event.code}`);
    }
    const ack = event.payload;
    if (event.threadId !== target.threadId || ack.command !== "MESSAGE_CREATE"
        || ack.clientMessageId !== clientMessageId || ack.deduplicated !== false) {
      throw new Error(`${phase} ACK의 요청 식별 정보가 다릅니다`);
    }
    progress.acknowledged = true;
    return ack;
  });
  const delivered = receiver.waitFor((frame) => {
    const event = eventOf(frame);
    return event?.type === "message.created" && event.threadId === target.threadId
      && event.payload?.clientMessageId === clientMessageId;
  }, `${phase} 상대방 수신`, timeoutMs).then((frame) => {
    const message = eventOf(frame).payload.message;
    if (message?.senderId !== target.senderId || message.threadId !== target.threadId
        || message.type !== "TEXT" || message.clientMessageId !== clientMessageId
        || message.content !== content) {
      throw new Error(`${phase} 수신 메시지의 발신자·본문·식별 정보가 다릅니다`);
    }
    progress.delivered = true;
    return message;
  });

  // 어느 한쪽이 실패해도 나머지 대기자의 결과를 회수해 처리되지 않은 Promise를 남기지 않는다.
  const pending = Promise.allSettled([acknowledged, delivered]);
  sender.send(`/app/community/threads/${target.threadId}/messages`,
    { clientMessageId, type: "TEXT", content }, { "x-command-id": commandId });
  const results = await pending;
  for (const result of results) {
    if (result.status === "rejected") throw result.reason;
  }
  if (results[0].value.messageId !== results[1].value.messageId) {
    throw new Error(`${phase} ACK와 수신 이벤트가 서로 다른 메시지를 가리킵니다`);
  }
  return results[1].value;
}

async function prepareSubscriptions(sender, receiver, target) {
  for (const client of [sender, receiver]) {
    client.subscribe(EVENTS);
    client.subscribe(ERRORS);
  }
  // SIMPLE broker는 구독 RECEIPT를 지원하지 않는다. 실제 준비용 송수신으로 두 구독이 유효함을 확인한다.
  // 구독 등록 경합으로 준비 메시지가 유실된 경우에만 최대 3번 시도하며, 본 테스트 메시지는 재시도하지 않는다.
  for (let attempt = 1; attempt <= 3; attempt++) {
    try {
      await exchange(sender, receiver, target, "준비", Math.min(TIMEOUT_MS, 3000));
      return;
    } catch (error) {
      if (!(error instanceof StompTimeoutError) || attempt === 3) throw error;
    }
  }
}

export default async function (seed) {
  const clients = [];
  const result = { connected: false, ready: false, acknowledged: false,
    delivered: false, correlated: false, closed: false };
  let failure = null;
  try {
    const target = validateSeed(seed);
    const url = `${BASE_URL.replace(/^http/, "ws")}/ws/websocket`;
    const sender = new StompClient(url, { token: getAccessToken(target.senderId),
      label: "sender", timeoutMs: TIMEOUT_MS });
    clients.push(sender);
    const receiver = new StompClient(url, { token: getAccessToken(target.receiverId),
      label: "receiver", timeoutMs: TIMEOUT_MS });
    clients.push(receiver);
    await Promise.all(clients.map((client) => client.connect()));
    result.connected = true;
    await prepareSubscriptions(sender, receiver, target);
    result.ready = true;
    const message = await exchange(sender, receiver, target, "Smoke", TIMEOUT_MS, result);
    result.correlated = true;
    console.log(`Chat Smoke 송수신 성공: threadId=${target.threadId}, messageId=${message.messageId}`);
  } catch (error) {
    failure = error;
  } finally {
    // 성공·실패 모두 heartbeat, 대기 타이머와 소켓을 정리해야 k6 실행이 끝난다.
    const closed = await Promise.allSettled(clients.map((client) => client.close()));
    result.closed = closed.every((entry) => entry.status === "fulfilled");
    if (!failure) failure = clients.find((client) => client.failure)?.failure || null;
    if (!failure && !result.closed) failure = closed.find((entry) => entry.status === "rejected").reason;
    check(result, {
      "두 회원 STOMP 인증 완료": (value) => value.connected,
      "준비 메시지로 구독 확인": (value) => value.ready,
      "발신자 ACK 일치": (value) => value.acknowledged,
      "상대방 메시지 수신 일치": (value) => value.delivered,
      "ACK와 수신 메시지 ID 일치": (value) => value.correlated,
      "모든 연결 종료": (value) => value.closed,
      "Smoke 오류 없음": () => failure === null,
    }, { scenario: "chat-message" });
  }
  if (failure) throw failure;
}
