import { scenario } from "k6/execution";
import { readLockConfig, lockSeed, lockOptions } from "../../config/chat-locks.js";
import { LockClient, lockRunFailures, pause, requireLockCheck } from "../../lib/chat/lock-client.js";
import { prepareLockRun } from "../../lib/chat/lock-prepare.js";
import { executeLockOperation } from "../../lib/chat/lock-operations.js";
import { runFixedSchedule } from "../../lib/chat/fixed-schedule.js";
import { recordSchedule } from "../../lib/chat/schedule-metrics.js";
import { StompTimeoutError } from "../../lib/stomp/stomp.js";

export const requiresSeed = true;
export const getOptions = () => lockOptions(readLockConfig(__ENV));
export const validateSeed = (seed) => lockSeed(seed, readLockConfig(__ENV));
export const prepareRun = (seed) => prepareLockRun(seed, readLockConfig(__ENV));

export function getRunConditions(seed, run) {
  return { config: readLockConfig(__ENV), rooms: run?.rooms, members: run?.members,
    timing: run?.timing, initialState: run?.initialState, dataModel: "lock-suite-v1",
    variantIsLabelOnly: true,
    limits: ["가입과 읽음 갱신의 상위 CommunityThread 락은 두 버전 모두 유지",
      "10명/50명은 요청을 발생시키는 회원 수. 메시지 작성자와 초대 대상은 별도 준비 데이터",
      "동일 회차에서 같은 방에 부하를 주고, 다음 회차에는 새 방을 사용"] };
}

export default async function (seed, run) {
  const config = readLockConfig(__ENV);
  const index = scenario.iterationInTest;
  const client = new LockClient(run.members.actors[index], config);
  client.initializeMetrics();
  try {
    await client.open();
    // 별도 준비 방의 멱등 요청으로 구독을 확인한다. 측정용 읽음 위치는 건드리지 않는다.
    for (let attempt = 0; attempt < 3; attempt++) {
      try {
        await client.command({ ...run.readiness, path: "/app/community/threads/" + run.readiness.threadId
          + "/messages/" + run.readiness.messageId + "/reactions/remove" }, "REACTION_REMOVE", { emoji: "👍" });
        break;
      } catch (error) { if (!(error instanceof StompTimeoutError) || attempt === 2) throw error; }
    }
    requireLockCheck(Date.now() < run.timing.warmupStartAt, "공통 시작 전 연결 준비 완료");
    const phases = [
      { phase: "warmup", start: run.timing.warmupStartAt, end: run.timing.warmupEndAt, offset: 0 },
      { phase: "measurement", start: run.timing.measurementStartAt,
        end: run.timing.measurementEndAt, offset: config.warmupRounds },
    ];
    for (const window of phases) {
      if (window.start === window.end) continue;
      requireLockCheck(Date.now() < window.start, "측정 구간 시작 시각 준수");
      await runFixedSchedule({ startAt: window.start,
        measureAt: window.phase === "warmup" ? window.end : window.start, endAt: window.end,
        intervalMs: config.intervalMs, offsetMs: 0, maxInflight: config.maxInflight }, {
        healthy() { if (client.failure) throw client.failure; },
        send: (slot) => executeLockOperation(client, { room: run.rooms[window.offset + slot],
          role: config.roles[index], index, invitee: run.members.invitees[index], phase: window.phase }),
        record: (sample) => recordSchedule(sample, { ...config.tags, phase: window.phase }),
      }, { now: () => Date.now(), pause });
    }
  } catch (error) {
    lockRunFailures.add(1, config.tags);
    throw error;
  } finally {
    try { await client.close(); }
    catch (error) { lockRunFailures.add(1, config.tags); throw error; }
  }
}
