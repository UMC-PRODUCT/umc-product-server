/** 실제 변경 ACK의 증감을 합산해, 응답 순서가 뒤집혀도 최종 저장 상태와 비교한다. */
export class ReactionIntegrity {
  constructor() {
    this.balance = 0;
    this.acknowledged = 0;
  }

  record(operation, deduplicated) {
    this.acknowledged++;
    if (deduplicated) return;
    switch (operation) {
      case "add": this.balance++; break;
      case "remove": this.balance--; break;
      default: throw new RangeError("지원하지 않는 리액션 작업입니다");
    }
  }

  matches(state) {
    return this.acknowledged > 0 && [0, 1].includes(this.balance)
      && state.count === this.balance && state.reactedByMe === (this.balance === 1);
  }
}
