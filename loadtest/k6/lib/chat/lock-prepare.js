import { lockSeed } from "../../config/chat-locks.js";
import { LockClient, pause, requireLockCheck } from "./lock-client.js";

export async function prepareLockRun(seed, config) {
  const members = lockSeed(seed, config);
  const owner = new LockClient(members.actors[0], config);
  const author = new LockClient(members.author, config);
  const runId = crypto.randomUUID();
  const rooms = [];
  const baseMembers = config.operation === "join" ? members.actors : [...members.actors, members.author];
  const joins = config.roles.map((role, index) => role === "join" ? index : -1).filter((index) => index >= 0);
  let readiness;
  const createRoom = async (label, ids) => {
    const room = await owner.request("POST", "/api/v1/community/threads", {
      title: "k6-lock " + runId + " " + label, description: "락 비교 전용 데이터", category: "FREE",
      icon: "💬", memberIds: ids.slice(1).map(Number),
    });
    requireLockCheck(typeof room.threadId === "string" && /^[1-9][0-9]*$/.test(room.threadId), "준비 방 ID 확인");
    return room.threadId;
  };
  const createMessage = async (threadId, label) => {
    const message = author.createMessage(threadId, { measured: false, content: "k6-lock " + runId + " " + label });
    const ack = await author.sendMessage(message);
    requireLockCheck(typeof ack.messageId === "string" && /^[1-9][0-9]*$/.test(ack.messageId), "준비 메시지 ID 확인");
    await pause(100);
    return ack.messageId;
  };
  try {
    await author.open();
    const threadId = await createRoom("ready", [...members.actors, members.author]);
    readiness = { threadId, messageId: await createMessage(threadId, "ready") };
    for (let round = 0; round < config.warmupRounds + config.measurementRounds; round++) {
      const threadId = await createRoom(String(round), baseMembers);
      for (const index of joins) {
        if (index === 0) continue;
        await owner.request("PATCH", `/api/v1/community/threads/${threadId}/members/${members.actors[index]}/role`,
          { role: "ADMIN" });
        // 준비 요청도 기본 API 제한(회원별 분당 300회) 안에서 수행한다.
        await pause(250);
      }
      const reactionMessageIds = {};
      for (let index = 0; index < config.users; index++) {
        if (config.roles[index] === "reaction") reactionMessageIds[index] = await createMessage(threadId, "reaction " + index);
      }
      const messageId = config.operation === "join" ? null : await createMessage(threadId, "read-report");
      const detail = await owner.request("GET", "/api/v1/community/threads/" + threadId);
      requireLockCheck(detail.isJoined === true && Number(detail.memberCount) === baseMembers.length,
        "회차별 초기 가입 인원 확인");
      rooms.push({ threadId, memberIds: baseMembers, messageId, reactionMessageIds,
        expectedMemberIds: [...baseMembers, ...joins.map((index) => members.invitees[index])] });
      if ((round + 1) % 10 === 0) console.log("락 비교 데이터 준비: " + (round + 1) + "개 방");
    }
  } finally { await author.close(); }
  const warmupStartAt = Date.now() + config.startupMs;
  const warmupEndAt = warmupStartAt + config.warmupMs;
  const measurementStartAt = warmupEndAt + config.timeoutMs * 2;
  return { runId, members, rooms, readiness, initialState: {
    schemaVersion: 1, roomCount: rooms.length, initialMembersPerRoom: baseMembers.length,
    finalMembersPerRoom: baseMembers.length + joins.length, roles: config.roles,
    messagesPerRoom: config.operation === "join" ? 0 : config.roles.filter((role) => role === "reaction").length + 1,
    readPositions: "actors-unread", reactions: 0, reports: 0, invitees: "absent",
    roomRotation: "one-fresh-shared-room-per-round", authorIsPassive: true,
  }, timing: { warmupStartAt, warmupEndAt, measurementStartAt,
    measurementEndAt: measurementStartAt + config.measurementMs } };
}
