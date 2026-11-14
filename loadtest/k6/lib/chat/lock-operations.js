import { LockSuiteError } from "../../config/chat-locks.js";
import { requireLockCheck } from "./lock-client.js";

export async function executeLockOperation(client, context) {
  const { room, role, index, invitee, phase } = context;
  const base = "/app/community/threads/" + room.threadId;
  switch (role) {
    case "reaction": {
      const messageId = room.reactionMessageIds[index];
      const target = { threadId: room.threadId, messageId };
      await client.measure("reaction_add", phase, () => client.command({ ...target,
        path: base + "/messages/" + messageId + "/reactions/add" }, "REACTION_ADD", { emoji: "👍" }));
      return client.measure("reaction_remove", phase, () => client.command({ ...target,
        path: base + "/messages/" + messageId + "/reactions/remove" }, "REACTION_REMOVE", { emoji: "👍" }));
    }
    case "read":
      return client.measure("read", phase, () => client.command({ threadId: room.threadId,
        messageId: room.messageId, path: base + "/read" }, "READ_UPDATE", { lastReadMessageId: room.messageId }));
    case "join":
      return client.measure("join", phase, async () => {
        const result = await client.request("POST", "/api/v1/community/threads/" + room.threadId + "/invite",
          { memberIds: [Number(invitee)] });
        requireLockCheck(result.invitedMembers?.length === 1
          && result.invitedMembers[0].memberId === invitee, "요청한 회원의 가입 성공");
        return result;
      });
    case "report":
      return client.measure("report", phase, async () => {
        const result = await client.request("POST", "/api/v1/community/messages/" + room.messageId + "/report",
          { reasons: ["SPAM"] });
        requireLockCheck(result.messageId === room.messageId && /^[1-9][0-9]*$/.test(result.reportId), "신고 저장 응답 확인");
        return result;
      });
    default: throw new LockSuiteError("지원하지 않는 작업입니다: " + role);
  }
}
