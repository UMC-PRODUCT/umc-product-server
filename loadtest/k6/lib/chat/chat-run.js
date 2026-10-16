/**
 * 시나리오의 조건 기록 함수가 반환한 객체에 실제 선택한 이름과 k6 실행 옵션을 붙인다.
 * getRunConditions(seed)는 기본값이 적용된 설정과 사용자 배정 등 JSON으로 저장할 정보만 동기적으로 반환한다.
 * 새 시나리오의 추가 설정도 이 객체에 포함한다. 인증 토큰이나 환경변수 전체를 반환하지 않는다.
 */
export function chatRunConditions({ profile, scenario, seed, options }, getRunConditions) {
  if (typeof getRunConditions !== "function") throw new Error("시나리오에 getRunConditions 함수가 필요합니다");
  const conditions = getRunConditions(seed);
  if (!conditions || typeof conditions !== "object" || Array.isArray(conditions)
      || typeof conditions.then === "function" || conditions.available === false) {
    throw new Error("getRunConditions는 실행 조건을 담은 객체를 동기적으로 반환해야 합니다");
  }
  return { ...conditions, profile, scenario, k6Options: options };
}
