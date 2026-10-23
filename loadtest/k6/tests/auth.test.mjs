import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test, { beforeEach } from "node:test";

globalThis.__ENV = { BASE_URL: "http://default-server:8080" };
globalThis.__authHarness = { requests: [], status: 200 };
registerHooks({
  resolve(specifier, context, next) {
    return specifier === "k6" || specifier.startsWith("k6/")
      ? { url: `mock:${specifier}`, shortCircuit: true } : next(specifier, context);
  },
  load(url, context, next) {
    const modules = {
      "mock:k6": "export const check = () => true;",
      "mock:k6/metrics": "class Metric { add() {} } export { Metric as Rate };",
      "mock:k6/http": `export default { get(url, params) {
        const state = globalThis.__authHarness;
        state.requests.push({url, params});
        return { status: state.status, body: 'token-' + state.requests.length + '-' + new URL(url).host };
      } };`,
    };
    return url in modules ? { format: "module", source: modules[url], shortCircuit: true } : next(url, context);
  },
});
const { getAccessToken } = await import("../lib/auth.js");
const state = globalThis.__authHarness;

beforeEach(() => { state.requests = []; state.status = 200; });

test("기존 호출은 전역 BASE_URL과 auth 태그를 유지한다", () => {
  getAccessToken("101");
  assert.equal(state.requests[0].url, "http://default-server:8080/test/token/access?memberId=101");
  assert.deepEqual(state.requests[0].params, { tags: { scenario: "auth" } });
});

test("클라이언트 서버로 인증하고 같은 서버·회원의 토큰만 재사용한다", () => {
  const a = getAccessToken("102", { baseUrl: "http://server-a:8080", timeout: "100ms" });
  const b = getAccessToken("102", { baseUrl: "http://server-b:8080" });
  const again = getAccessToken("102", { baseUrl: "http://server-a:8080/" });
  assert.notEqual(a, b);
  assert.equal(again, a);
  assert.equal(state.requests.length, 2);
  assert.equal(state.requests[0].url, "http://server-a:8080/test/token/access?memberId=102");
  assert.equal(state.requests[1].url, "http://server-b:8080/test/token/access?memberId=102");
  assert.equal(state.requests[0].params.timeout, "100ms");
});

test("같은 서버라도 회원이 다르면 토큰을 새로 발급한다", () => {
  getAccessToken("103", { baseUrl: "http://server-c:8080" });
  getAccessToken("104", { baseUrl: "http://server-c:8080" });
  assert.equal(state.requests.length, 2);
});

test("실패한 토큰 응답은 캐시하지 않는다", () => {
  state.status = 500;
  assert.throws(() => getAccessToken("105"), /토큰 발급 실패/);
  state.status = 200;
  assert.ok(getAccessToken("105"));
  assert.equal(state.requests.length, 2);
});
