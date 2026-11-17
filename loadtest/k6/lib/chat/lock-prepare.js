import clock from "k6/x/monotonic";
import { lockSeed } from "../../config/chat-locks.js";
import { LockClient, pause, requireLockCheck } from "./lock-client.js";

export async function prepareLockRun(seed, config) {
  const members = lockSeed(seed, config);
  const owner = new LockClient(members.actors[0], config);
  const author = new LockClient(members.author, config);
  const runId = crypto.randomUUID();
  const rooms = [];
  const rounds = [];
  const rotateRooms = config.operation === "join" || config.operation === "mixed";
  const baseMembers = config.operation === "join" ? members.actors : [...members.actors, members.author];
  const joins = config.roles.map((role, index) => role === "join" ? index : -1).filter((index) => index >= 0);
  let readiness;
  let nextMessageAt = 0;
  const createRoom = async (label, ids) => {
    const room = await owner.request("POST", "/api/v1/community/threads", {
      title: "k6-lock " + runId + " " + label, description: "락 비교 전용 데이터", category: "FREE",
      icon: "💬", memberIds: ids.slice(1).map(Number),
    });
    requireLockCheck(typeof room.threadId === "string" && /^[1-9][0-9]*$/.test(room.threadId), "준비 방 ID 확인");
    return room.threadId;
  };
  const createMessage = async (threadId, label) => {
    // 준비 메시지도 회원별 초당 SEND 20회 제한 안에서 보낸다.
    await pause(nextMessageAt - clock.now());
    nextMessageAt = clock.now() + 60;
    const message = author.createMessage(threadId, { measured: false, content: "k6-lock " + runId + " " + label });
    const ack = await author.sendMessage(message);
    requireLockCheck(typeof ack.messageId === "string" && /^[1-9][0-9]*$/.test(ack.messageId), "준비 메시지 ID 확인");
    return ack.messageId;
  };
  try {
    await author.open();
    const threadId = await createRoom("ready", [...members.actors, members.author]);
    readiness = { threadId, messageId: await createMessage(threadId, "ready") };
    let sharedRoom;
    for (let round = 0; round < config.warmupRounds + config.measurementRounds; round++) {
      let room = sharedRoom;
      if (rotateRooms || !room) {
        const threadId = await createRoom(String(round), baseMembers);
        for (const index of joins) {
          if (index === 0) continue;
          await owner.request("PATCH", `/api/v1/community/threads/${threadId}/members/${members.actors[index]}/role`,
            { role: "ADMIN" });
          // 권한 변경은 회원별 기본 분당 300회 제한 안에서 준비한다.
          await pause(250);
        }
        const reactionMessageIds = {};
        for (let index = 0; index < config.users; index++) {
          if (config.roles[index] === "reaction") {
            reactionMessageIds[index] = await createMessage(threadId, "reaction " + index);
          }
        }
        room = { threadId, memberIds: baseMembers, messageId: null, messageIds: [], reactionMessageIds,
          expectedMemberIds: [...baseMembers, ...joins.map((index) => members.invitees[index])] };
        rooms.push(room);
        if (!rotateRooms) sharedRoom = room;
        const detail = await owner.request("GET", "/api/v1/community/threads/" + threadId);
        requireLockCheck(detail.isJoined === true && Number(detail.memberCount) === baseMembers.length,
          "측정 방의 초기 가입 인원 확인");
      }
      let messageId = null;
      if (config.roles.some((role) => role === "read" || role === "report")) {
        messageId = await createMessage(room.threadId, "read-report " + round);
        room.messageIds.push(messageId);
        room.messageId = messageId;
      }
      rounds.push({ threadId: room.threadId, messageId, reactionMessageIds: room.reactionMessageIds });
      if ((round + 1) % 10 === 0) console.log("락 비교 데이터 준비: " + (round + 1) + "회차, " + rooms.length + "개 방");
    }
  } finally { await author.close(); }
  const warmupStartAt = clock.now() + config.startupMs;
  const warmupEndAt = warmupStartAt + config.warmupMs;
  const measurementStartAt = warmupEndAt + config.timeoutMs * 2;
  return { runId, members, rooms, rounds, readiness, initialState: {
    schemaVersion: 2, roomCount: rooms.length, roundCount: rounds.length, initialMembersPerRoom: baseMembers.length,
    finalMembersPerRoom: baseMembers.length + joins.length, roles: config.roles,
    messagesPerRoom: Object.keys(rooms[0].reactionMessageIds).length + rooms[0].messageIds.length,
    readPositions: "actors-unread", reactions: 0, reports: 0, invitees: "absent",
    roomRotation: rotateRooms ? "one-fresh-shared-room-per-round" : "one-shared-room-for-all-rounds",
    authorIsPassive: true,
  }, timing: { warmupStartAt, warmupEndAt, measurementStartAt,
    measurementEndAt: measurementStartAt + config.measurementMs } };
}
