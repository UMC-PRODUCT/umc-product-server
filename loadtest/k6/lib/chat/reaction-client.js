import http from "k6/http";

import { getAccessToken } from "../auth.js";
import { ChatClient } from "./chat-client.js";
import { StompError, StompTimeoutError } from "../stomp/stomp.js";
import { reactionAttempt, reactionResult, reactionNoop } from "./reaction-metrics.js";
import { ReactionIntegrity } from "./reaction-integrity.js";

export class ReactionClient extends ChatClient {
  constructor(target, config, label) {
    super(target.memberId, config, { label });
    this.target = target;
    this.integrity = new ReactionIntegrity();
  }

  async changeReaction(operation, phase = "prepare", timeoutMs = this.config.timeoutMs) {
    if (!["add", "remove"].includes(operation)) throw new StompError("리액션 작업이 올바르지 않습니다", "mismatch");
    const commandId = crypto.randomUUID();
    const measured = phase === "measure";
    const tags = { ...this.config.tags, operation };
    const entry = this.track("ack", commandId, { measured });
    const pending = this.stomp.waitFor((frame) => {
      if (frame.command !== "MESSAGE") return false;
      const event = frame.event || JSON.parse(frame.body);
      return event.payload?.commandId === commandId || event.commandId === commandId;
    }, "리액션 ACK", timeoutMs, commandId);
    const startedAt = Date.now();
    if (phase !== "prepare") reactionAttempt(tags, measured);
    try {
      try {
        this.stomp.send("/app/community/threads/" + this.target.threadId + "/messages/"
          + this.target.messageId + "/reactions/" + operation,
        { emoji: this.config.emoji }, { "x-command-id": commandId });
      } catch (error) {
        this.stomp.cancelWaiters(commandId, error);
        await pending.catch((failure) => {
          if (failure !== error) throw failure;
        });
        throw new StompError("리액션 전송 실패", "send_error");
      }
      const frame = await pending;
      const event = frame.event || JSON.parse(frame.body);
      if (event.type !== "command.acknowledged") throw new StompError("리액션 요청 거절", "rejected");
      const ack = event.payload;
      if (event.threadId !== this.target.threadId || ack.messageId !== this.target.messageId
          || ack.command !== "REACTION_" + operation.toUpperCase() || typeof ack.deduplicated !== "boolean") {
        throw new StompError("리액션 ACK의 식별 정보가 다릅니다", "mismatch");
      }
      if (measured) {
        reactionResult(tags, frame.receivedAt - startedAt, true);
        this.integrity.record(operation, ack.deduplicated);
        if (ack.deduplicated) reactionNoop(tags);
      }
      return ack;
    } catch (error) {
      if (measured) reactionResult(tags, 0, false);
      throw error;
    } finally { entry.finished = true; }
  }

  async prepare() {
    await this.open();
    // 구독 완료는 실제 ACK로 확인한다. 멱등인 제거만 구독 준비 시간 초과 때 재시도한다.
    for (let attempt = 0; attempt < 3; attempt++) {
      try { await this.changeReaction("remove", "prepare", Math.min(this.config.timeoutMs, 1000)); return; }
      catch (error) { if (!(error instanceof StompTimeoutError) || attempt === 2) throw error; }
    }
  }

  async read(path) {
    const token = getAccessToken(this.memberId, { baseUrl: this.config.baseUrl,
      timeout: String(this.config.timeoutMs) + "ms" });
    const response = await http.asyncRequest("GET", this.config.baseUrl + path, null, {
      headers: { Authorization: "Bearer " + token }, timeout: String(this.config.timeoutMs) + "ms",
      tags: { scenario: "reaction-verification" }, redirects: 0,
    });
    if (response.status !== 200) throw new StompError("리액션 준비 또는 결과 조회 실패", "rejected");
    let body;
    try { body = response.json(); }
    catch (error) { if (error instanceof SyntaxError) throw new StompError("조회 JSON 형식 오류", "mismatch"); throw error; }
    if (body?.success !== true || !body.result) throw new StompError("조회 응답 계약 오류", "mismatch");
    return body.result;
  }

  async getReaction() {
    // 새 메시지가 계속 쌓여도 대상 메시지 바로 다음 ID부터 조회해 최신 목록을 전부 훑지 않는다.
    let before = String(BigInt(this.target.messageId) + 1n);
    for (let page = 0; page < 20; page++) {
      const result = await this.read("/api/v1/community/threads/" + this.target.threadId
        + "/messages?limit=100" + (before ? "&before=" + before : ""));
      if (!Array.isArray(result.messages)) throw new StompError("메시지 목록 응답 오류", "mismatch");
      const message = result.messages.find((item) => item.messageId === this.target.messageId);
      if (message) {
        if (message.deletedAt || !Array.isArray(message.reactions)) throw new StompError("리액션 대상 메시지 상태 오류", "mismatch");
        const reaction = message.reactions.find((item) => item.emoji === this.config.emoji);
        if (!reaction) return { count: 0, reactedByMe: false };
        if (!/^[1-9][0-9]*$/.test(String(reaction.count)) || typeof reaction.reactedByMe !== "boolean") {
          throw new StompError("리액션 집계 응답 오류", "mismatch");
        }
        return { count: Number(reaction.count), reactedByMe: reaction.reactedByMe };
      }
      if (!result.hasMore) break;
      if (typeof result.nextBefore !== "string" || !/^[1-9][0-9]*$/.test(result.nextBefore)
          || result.nextBefore === before) throw new StompError("메시지 조회 커서 오류", "mismatch");
      before = result.nextBefore;
    }
    throw new StompError("시드의 대상 메시지를 찾지 못했습니다", "mismatch");
  }
}
