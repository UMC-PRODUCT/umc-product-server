// ADR-013: 운영 앱에는 직접 부하를 걸지 않는다. 부하 대상은 dev와 Preview 앱뿐이다.
const PRODUCTION_API_HOST = "api.university.neordinary.com";

// k6에는 WHATWG URL이 없을 수 있어 문자열로 host만 뽑는다. 사용자 정보·포트·끝의 점은 떼고 비교한다.
function hostOf(url) {
  const authority = url.replace(/^[a-z][a-z0-9+.-]*:\/\//i, "").split(/[/?#]/)[0];
  return authority.slice(authority.lastIndexOf("@") + 1).replace(/:[0-9]*$/, "").replace(/\.$/, "").toLowerCase();
}

// 부하를 보낼 앱 주소를 경로를 붙일 수 있는 형태로 정리하고, 운영 API면 실행을 멈춘다.
export function loadTestOrigin(url) {
  const origin = url.replace(/\/+$/, "");
  if (hostOf(origin) === PRODUCTION_API_HOST) {
    throw new Error(`운영 API(${PRODUCTION_API_HOST})에는 부하를 걸 수 없습니다. dev나 Preview 앱 주소를 지정하세요 (ADR-013)`);
  }
  return origin;
}
