/** ACK 완료와 독립적으로 정해진 슬롯마다 전송한다. 놓친 슬롯은 몰아서 보내지 않는다. */
export async function runFixedSchedule(plan, actions, clock = {
  now: () => Date.now(),
  pause: (ms) => new Promise((resolve) => setTimeout(resolve, Math.max(0, ms))),
}) {
  const pending = new Set();
  const results = [];
  let failure;
  const phases = [
    { phase: "warmup", start: plan.startAt, end: plan.measureAt },
    { phase: "measurement", start: plan.measureAt, end: plan.endAt },
  ];
  try {
    for (const { phase, start, end } of phases) {
      let issued = 0;
      const planned = Math.max(0, Math.ceil((end - start - plan.offsetMs) / plan.intervalMs));
      actions.record({ phase, planned, issued: 0, dropped: 0, pending: pending.size });
      try {
        for (let slot = 0; slot < planned; slot++) {
          const due = start + plan.offsetMs + slot * plan.intervalMs;
          // 타이머가 일찍 깨어나거나 시스템 시계가 뒤로 움직이면 예정 시각까지 다시 기다린다.
          do { await clock.pause(due - clock.now()); } while (clock.now() < due);
          actions.healthy();
          const lag = clock.now() - due;
          if (clock.now() >= end || lag >= plan.intervalMs || pending.size >= plan.maxInflight) continue;
          issued++;
          const task = (async () => actions.send(slot, phase))().catch((error) => {
            // no-excuse-ok: catch - 전송 경계에서 실패를 모아 마지막에 다시 던진다.
            failure ??= error;
          }).finally(() => {
            pending.delete(task);
            actions.record({ phase: clock.now() < plan.measureAt ? "warmup" : "measurement", pending: pending.size });
          });
          pending.add(task);
          actions.record({ phase, issued: 1, pending: pending.size, lag });
        }
        await clock.pause(end - clock.now());
      } finally {
        actions.record({ phase, dropped: planned - issued });
        results.push({ phase, planned, issued, dropped: planned - issued });
      }
    }
    actions.record({ phase: "measurement", pendingAtEnd: pending.size });
  } finally {
    await Promise.allSettled(pending);
  }
  if (failure) throw failure;
  return results;
}
