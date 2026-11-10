import http from "k6/http";
import { check } from "k6";

import { errorRate } from "./metrics.js";
import { loadTestOrigin } from "./target.js";

// BASE_URL 환경변수로 부하를 보낼 앱 주소를 지정한다. 운영 API면 init 단계에서 실패한다.
const BASE_URL = loadTestOrigin(__ENV.BASE_URL || "http://localhost:8080");

// dev·Preview의 /test/**는 Traefik Basic 인증 뒤에 있다. 회원 토큰이 있으면 그 토큰으로 인증한다.
export function authorizationHeaders(path, token) {
  if (token) return { Authorization: `Bearer ${token}` };
  if (path.startsWith("/test/") && __ENV.TEST_API_BASIC_AUTH) {
    return { Authorization: `Basic ${__ENV.TEST_API_BASIC_AUTH}` };
  }
  return {};
}

function headers(path, token) {
  return { "Content-Type": "application/json", ...authorizationHeaders(path, token) };
}

// 공통 GET: 상태코드 check + 실패를 scenario 태그로 errorRate 에 적재한다.
export function get(path, { token, tag, expectedStatus = 200 } = {}) {
  const label = tag || path;
  const res = http.get(`${BASE_URL}${path}`, {
    headers: headers(path, token),
    tags: { scenario: label },
  });
  const ok = check(res, {
    [`${label} status ${expectedStatus}`]: (r) => r.status === expectedStatus,
  });
  errorRate.add(!ok, { scenario: label });
  return res;
}

// 공통 POST: body 를 JSON 직렬화해 보낸다.
export function post(path, body, { token, tag, expectedStatus = 200 } = {}) {
  const label = tag || path;
  const res = http.post(`${BASE_URL}${path}`, JSON.stringify(body), {
    headers: headers(path, token),
    tags: { scenario: label },
  });
  const ok = check(res, {
    [`${label} status ${expectedStatus}`]: (r) => r.status === expectedStatus,
  });
  errorRate.add(!ok, { scenario: label });
  return res;
}

// 여러 GET을 홈 화면처럼 동시에(병렬) 발사한다. 각 응답을 개별 태깅·체크한다.
// reqs: [{ path, tag, expectedStatus }]
export function batchGet(token, reqs) {
  const requests = reqs.map((r) => ({
    method: "GET",
    url: `${BASE_URL}${r.path}`,
    params: { headers: headers(r.path, token), tags: { scenario: r.tag } },
  }));
  const responses = http.batch(requests);
  responses.forEach((res, i) => {
    const label = reqs[i].tag;
    const expected = reqs[i].expectedStatus || 200;
    const ok = check(res, {
      [`${label} status ${expected}`]: (r) => r.status === expected,
    });
    errorRate.add(!ok, { scenario: label });
  });
  return responses;
}

export { BASE_URL };
