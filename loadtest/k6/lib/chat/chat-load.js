const id = (value) => typeof value === "string" && /^[1-9][0-9]*$/.test(value);
const key = (threadId, senderId) => `${threadId}:${senderId}`;
const mismatch = () => Object.assign(new Error("부하 메시지의 식별 정보 또는 순서가 올바르지 않습니다"),
  { reason: "mismatch" });

export function createLoadPlan(seed, config) {
  if (seed.baseUrl !== config.baseUrl) throw new Error("시드와 BASE_URL이 다릅니다");
  const members = seed.memberIds;
  const rooms = seed.rooms;
  if (!Array.isArray(members) || members.length !== config.users
      || !Array.isArray(rooms) || rooms.length !== config.rooms) {
    throw new Error("CHAT_USERS·CHAT_ROOMS와 시드의 회원·방 수가 일치해야 합니다");
  }
  if (members.some((member) => !id(member)) || new Set(members).size !== members.length) {
    throw new Error("시드의 회원 ID는 중복 없는 양의 정수 문자열이어야 합니다");
  }
  const threadIds = new Set();
  for (const room of rooms) {
    if (!id(room.threadId) || threadIds.has(room.threadId) || !Array.isArray(room.memberIds)
        || room.memberIds.length < 1 || new Set(room.memberIds).size !== room.memberIds.length
        || room.memberIds.some((member) => !members.includes(member))) {
      throw new Error("방 ID·참여자 목록이 올바르지 않습니다");
    }
    threadIds.add(room.threadId);
  }
  return members.map((memberId) => {
    const joined = rooms.filter((room) => room.memberIds.includes(memberId));
    if (!joined.length) throw new Error("각 사용자는 적어도 한 방에 참여해야 합니다");
    return { memberId, rooms: joined };
  });
}

export function loadContent(runId, kind, message, sentAt, sequence = 0) {
  // 앱의 payload 계약은 그대로 사용하고 TEXT 본문 안에 측정용 정보를 넣는다.
  return JSON.stringify({ __k6_chat: { version: 1, runId, kind, sentAt, sequence,
    threadId: message.threadId, senderId: message.senderId, clientMessageId: message.clientMessageId } });
}

/**
 * 반복 부하에서 수신자 한 명이 참여한 방별·발신자별 전달 상태를 추적한다. 연결이나 전송은 수행하지 않는다.
 * 대상 사용자와 공통 runId로 생성한 뒤, ChatClient.onEvent에서 observe(event, receivedAt)를 호출한다.
 * ready·armed로 모든 참여자의 수신 준비를 확인하고, observe의 반환값으로 전달 지연·중복을 집계한다.
 * 종료 시 summary()가 ACK 완료 순번에 따른 예상·누락 건수와 완료 신호 누락을 반환한다.
 * 다른 VU의 전송 시각은 메시지 본문에서 읽는다. 지연 비교를 위해 부하 발생기들의 시계가 맞아야 한다.
 */
export class DeliveryTracker {
  constructor(target, runId, timeoutMs) {
    this.target = target;
    this.runId = runId;
    this.timeoutMs = timeoutMs;
    this.streams = new Map();
    for (const room of target.rooms) {
      for (const senderId of room.memberIds) {
        this.streams.set(key(room.threadId, senderId),
          { senderId, ready: false, armed: false, done: null, through: 0, gaps: new Set() });
      }
    }
  }

  observe(event, receivedAt) {
    if (event.type !== "message.created") return null;
    const message = event.payload?.message;
    let wire;
    try { wire = JSON.parse(message?.content).__k6_chat; } catch (_) { return null; }
    if (!wire || wire.runId !== this.runId) return null;
    const stream = this.streams.get(key(wire.threadId, wire.senderId));
    if (!stream || wire.version !== 1 || event.threadId !== wire.threadId
        || message.threadId !== wire.threadId || message.senderId !== wire.senderId
        || message.type !== "TEXT" || event.payload.clientMessageId !== wire.clientMessageId
        || message.clientMessageId !== wire.clientMessageId || !Number.isSafeInteger(wire.sentAt)
        || wire.sentAt < 1 || !Number.isSafeInteger(wire.sequence) || wire.sequence < 0) throw mismatch();
    if (["ready", "armed"].includes(wire.kind)) {
      stream[wire.kind] = true;
      return null;
    }
    if (wire.kind === "done") {
      if (stream.done !== null && stream.done !== wire.sequence) throw mismatch();
      stream.done = wire.sequence;
      return null;
    }
    if (wire.kind !== "message" || wire.sequence < 1) throw mismatch();
    if (wire.senderId === this.target.memberId) return null;
    if (wire.sequence <= stream.through || stream.gaps.has(wire.sequence)) return { duplicate: true };
    const latencyMs = receivedAt - wire.sentAt;
    if (!Number.isFinite(latencyMs) || latencyMs < 0) throw mismatch();
    // 순서가 뒤집힌 수신도 집계하되, 누락 때문에 메모리가 무제한 증가하지 않도록 제한한다.
    if (wire.sequence === stream.through + 1) {
      stream.through++;
      while (stream.gaps.delete(stream.through + 1)) stream.through++;
    } else {
      if (stream.gaps.size >= 512) throw mismatch();
      stream.gaps.add(wire.sequence);
    }
    return { duplicate: false, latencyMs, late: latencyMs > this.timeoutMs };
  }

  get ready() { return [...this.streams.values()].every((stream) => stream.ready); }
  get armed() { return [...this.streams.values()].every((stream) => stream.armed); }

  summary() {
    let expected = 0, missing = 0, completionMissing = 0;
    for (const stream of this.streams.values()) {
      if (stream.done === null) { completionMissing++; continue; }
      if (stream.senderId === this.target.memberId) continue;
      // 송신자가 ACK를 확인한 마지막 순번까지 수신자가 받았는지 확인한다.
      const received = Math.min(stream.through, stream.done)
        + [...stream.gaps].filter((sequence) => sequence <= stream.done).length;
      expected += stream.done;
      missing += stream.done - received;
    }
    return { expected, missing, completionMissing };
  }
}
