import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test, { beforeEach } from "node:test";

globalThis.__ENV = { BASE_URL: "https://api-dev.university.neordinary.com/" };
globalThis.__httpHarness = { requests: [] };
registerHooks({
  resolve(specifier, context, next) {
    return specifier === "k6" || specifier.startsWith("k6/")
      ? { url: `mock:${specifier}`, shortCircuit: true } : next(specifier, context);
  },
  load(url, context, next) {
    const modules = {
      "mock:k6": "export const check = () => true;",
      "mock:k6/metrics": "class Metric { add() {} } export { Metric as Rate };",
      "mock:k6/http": `const sent = (url, params) => {
        globalThis.__httpHarness.requests.push({ url, headers: params.headers });
        return { status: 200 };
      };
      export default {
        get: (url, params) => sent(url, params),
        post: (url, body, params) => sent(url, params),
        batch: (reqs) => reqs.map((r) => sent(r.url, r.params)),
      };`,
    };
    return url in modules ? { format: "module", source: modules[url], shortCircuit: true } : next(url, context);
  },
});
const { get, post, batchGet } = await import("../lib/http.js");
const state = globalThis.__httpHarness;
const authorizations = () => state.requests.map((r) => r.headers.Authorization);

beforeEach(() => { state.requests = []; delete __ENV.TEST_API_BASIC_AUTH; });

test("토큰 없는 /test/ 요청에는 지정한 Basic 인증을 붙인다", () => {
  __ENV.TEST_API_BASIC_AUTH = "dGVzdDp0ZXN0";
  get("/test/health-check", { tag: "health-check" });
  post("/test/seed", {});
  assert.equal(state.requests[0].url, "https://api-dev.university.neordinary.com/test/health-check");
  assert.deepEqual(authorizations(), ["Basic dGVzdDp0ZXN0", "Basic dGVzdDp0ZXN0"]);
});

test("Basic 인증 값이 없거나 /test/ 밖의 경로면 인증 헤더를 붙이지 않는다", () => {
  get("/test/health-check");
  __ENV.TEST_API_BASIC_AUTH = "dGVzdDp0ZXN0";
  get("/api/v1/gisu/active");
  get("/testing");
  assert.deepEqual(authorizations(), [undefined, undefined, undefined]);
});

test("회원 토큰이 있으면 Bearer 토큰으로 인증한다", () => {
  get("/test/health-check", { token: "member-token" });
  __ENV.TEST_API_BASIC_AUTH = "dGVzdDp0ZXN0";
  batchGet("member-token", [{ path: "/api/v1/a", tag: "a" }, { path: "/api/v1/b", tag: "b" }]);
  assert.deepEqual(authorizations(), ["Bearer member-token", "Bearer member-token", "Bearer member-token"]);
});

test("Basic 인증 뒤의 /test/ 경로에 회원 토큰을 보내면 요청 전에 실패한다", () => {
  __ENV.TEST_API_BASIC_AUTH = "dGVzdDp0ZXN0";
  assert.throws(() => get("/test/health-check", { token: "member-token" }), /회원 토큰을 함께 보낼 수 없습니다/);
  assert.equal(authorizations().length, 0);
});

test("BASE_URL이 운영 API면 요청을 보내기 전 init 단계에서 실패한다", async () => {
  const production = ["https://api.university.neordinary.com", "https://API.University.Neordinary.com/",
    "https://api.university.neordinary.com:443", "http://user@api.university.neordinary.com",
    "https://api.university.neordinary.com./"];
  for (const [index, url] of production.entries()) {
    __ENV.BASE_URL = url;
    await assert.rejects(import(`../lib/http.js?production=${index}`), /운영 API/, url);
  }
  const allowed = ["https://api-dev.university.neordinary.com", "https://api-pr-12.preview.university.neordinary.com",
    "https://api.university.neordinary.com.example.org", "http://localhost:8080"];
  for (const [index, url] of allowed.entries()) {
    __ENV.BASE_URL = url;
    await import(`../lib/http.js?allowed=${index}`);
  }
});
