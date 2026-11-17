// 사용자별 요청을 순서대로 완료하며, 지연된 회차도 건너뛰거나 몰아 보내지 않는다.
export async function runLockRounds(plan, actions, clock) {
  let nextAt = Math.max(plan.startAt, clock.now());
  let startedAt;
  let issued = 0;
  actions.record({ planned: plan.rounds, issued: 0, dropped: 0, pending: 0 });
  try {
    for (let slot = 0; slot < plan.rounds; slot++) {
      do { await clock.pause(nextAt - clock.now()); } while (clock.now() < nextAt);
      actions.healthy();
      const sentAt = clock.now();
      startedAt ??= sentAt;
      issued++;
      actions.record({ issued: 1, pending: 1, lag: sentAt - nextAt });
      await actions.send(slot);
      actions.record({ pending: 0 });
      nextAt = sentAt + plan.intervalMs;
    }
    do { await clock.pause(nextAt - clock.now()); } while (clock.now() < nextAt);
    actions.record({ pendingAtEnd: 0 });
    return { startedAt, endedAt: clock.now() };
  } finally {
    actions.record({ dropped: plan.rounds - issued });
  }
}
