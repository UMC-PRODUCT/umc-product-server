export function repeatMessages(intervalMs) {
  return {
    async act({ target, run, sendMessage, pause, healthy }) {
      let index = 0;
      while (Date.now() < run.endAt) {
        healthy();
        const startedAt = Date.now();
        await sendMessage(target.rooms[index++ % target.rooms.length]);
        // 사용자별 전송 간격이다. 이전 ACK가 늦으면 실제 전송 빈도는 낮아진다.
        await pause(Math.min(startedAt + intervalMs, run.endAt) - Date.now());
      }
    },
    checks: { "측정 메시지 ACK 한 건 이상": (state) => state.sent > 0 },
  };
}

export function receiveMessages() {
  return {
    // 측정 메시지는 보내지 않고 수신을 기다린다. 구독·완료 신호는 공통 실행부에서 처리한다.
    async act({ waitUntilEnd }) { await waitUntilEnd(); },
    checks: { "측정 메시지 수신 한 건 이상": (state) => state.received > 0 },
  };
}
