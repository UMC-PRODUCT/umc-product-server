import { scenario } from "k6/execution";
import clock from "k6/x/monotonic";
import { readLockConfig, lockSeed, lockOptions } from "../../config/chat-locks.js";
import { LockClient, lockRunFailures, lockMeasurementStart, lockMeasurementEnd,
  pause, requireLockCheck } from "../../lib/chat/lock-client.js";
import { prepareLockRun } from "../../lib/chat/lock-prepare.js";
import { executeLockOperation } from "../../lib/chat/lock-operations.js";
import { runLockRounds } from "../../lib/chat/lock-schedule.js";
import { recordSchedule } from "../../lib/chat/schedule-metrics.js";
import { StompTimeoutError } from "../../lib/stomp/stomp.js";

export const requiresSeed = true;
export const getOptions = () => lockOptions(readLockConfig(__ENV));
export const validateSeed = (seed) => lockSeed(seed, readLockConfig(__ENV));
export const prepareRun = (seed) => prepareLockRun(seed, readLockConfig(__ENV));

export function getRunConditions(seed, run, summary) {
  const startedAt = summary?.metrics?.umc_chat_lock_measurement_start_ms?.values.min;
  const endedAt = summary?.metrics?.umc_chat_lock_measurement_end_ms?.values.max;
  const measuredTiming = Number.isFinite(startedAt) && Number.isFinite(endedAt);
  return { config: readLockConfig(__ENV), rooms: run?.rooms, rounds: run?.rounds, members: run?.members,
    timing: measuredTiming ? { ...run.timing,
      plannedMeasurementStartAt: run.timing.measurementStartAt, plannedMeasurementEndAt: run.timing.measurementEndAt,
      measurementStartAt: startedAt, measurementEndAt: endedAt } : run?.timing,
    timingSource: measuredTiming ? "measured" : "planned-incomplete",
    timingClock: "k6/x/monotonic: fixed Unix origin + Go monotonic elapsed",
    initialState: run?.initialState, dataModel: "lock-suite-v1",
    httpConnectionPreparation: { roles: ["join", "report"], leadMs: 5000,
      phases: ["warmup", "measurement"], mustFinishBeforeStart: true },
    variantIsLabelOnly: true,
    limits: ["가입과 읽음 갱신의 상위 CommunityThread 락은 두 버전 모두 유지",
      "10명/50명은 요청을 발생시키는 회원 수. 메시지 작성자와 초대 대상은 별도 준비 데이터",
      "리액션, 읽음, 신고는 같은 방을 재사용. 참여와 혼합은 회차마다 새 방을 사용",
      "모든 작업은 사용자별 이전 회차 완료 후 전송하며 시작 간격은 최소 2.5초(기본값)",
      "지연된 회차도 순서대로 실행하므로 실제 측정 시간이 늘 수 있으며 실제 구간으로 처리량을 계산"] };
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
    requireLockCheck(clock.now() < run.timing.warmupStartAt, "공통 시작 전 연결 준비 완료");
    const phases = [
      { phase: "warmup", start: run.timing.warmupStartAt, end: run.timing.warmupEndAt, offset: 0 },
      { phase: "measurement", start: run.timing.measurementStartAt,
        end: run.timing.measurementEndAt, offset: config.warmupRounds },
    ];
    for (const window of phases) {
      if (window.start === window.end) continue;
      if (["join", "report"].includes(config.roles[index])) {
        await client.prepareHttpConnection(run.rounds[window.offset].threadId, window.start, window.phase);
      }
      const timing = await runLockRounds({ startAt: window.start,
        rounds: window.phase === "warmup" ? config.warmupRounds : config.measurementRounds,
        intervalMs: config.intervalMs }, {
        healthy() { if (client.failure) throw client.failure; },
        send: (slot) => executeLockOperation(client, { room: run.rounds[window.offset + slot],
          role: config.roles[index], index, invitee: run.members.invitees[index], phase: window.phase }),
        record: (sample) => recordSchedule(sample, { ...config.tags, phase: window.phase }),
      }, { now: () => clock.now(), pause });
      if (window.phase === "measurement") {
        lockMeasurementStart.add(timing.startedAt, config.tags);
        lockMeasurementEnd.add(timing.endedAt, config.tags);
      }
    }
  } catch (error) {
    lockRunFailures.add(1, config.tags);
    throw error;
  } finally {
    try { await client.close(); }
    catch (error) { lockRunFailures.add(1, config.tags); throw error; }
  }
}
