import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test from "node:test";

globalThis.__ENV = { PROFILE: "load", SCENARIO: "chat-message", CHAT_RUN_DIR: "/runs/example" };
globalThis.open = () => JSON.stringify({ baseUrl: "http://localhost:8080", memberIds: ["1", "2"],
  rooms: [{ threadId: "10", memberIds: ["1", "2"] }] });
registerHooks({
  resolve(specifier, context, next) {
    if (specifier.startsWith("https://")) return { url: "mock:summary", shortCircuit: true };
    if (context.parentURL?.endsWith("/script.js") && specifier.startsWith("./scenarios/")) {
      return { url: "mock:scenario", shortCircuit: true };
    }
    return next(specifier, context);
  },
  load(url, context, next) {
    if (url === "mock:summary") return { format: "module", shortCircuit: true,
      source: 'export const textSummary = () => "요약";' };
    if (url === "mock:scenario") return { format: "module", shortCircuit: true, source: `
      export default function() {}
      export const requiresSeed = true;
      export const getOptions = () => ({ scenarios: {
        chat_load: { executor: "per-vu-iterations", vus: 2, iterations: 1 }
      } });
      export function validateSeed() {}
      export function prepareRun() {}
    ` };
    return next(url, context);
  },
});
const { handleSummary, options } = await import("../script.js");

test("k6가 실행 옵션 객체를 바꿔도 최초 시나리오 설정을 보존한다", () => {
  options.scenarios = {};
  const output = handleSummary({ options: { noColor: true }, metrics: {} });
  const conditions = JSON.parse(output["/runs/example/conditions.json"]);
  assert.equal(conditions.k6Options.scenarios.chat_load.vus, 2);
  assert.deepEqual(conditions.vuMemberIds, [["1"], ["2"]]);
  assert.ok(output["/runs/example/summary.json"]);
});

test("조건 계산에 실패해도 요약과 실패 안내를 보존하고 직접 실행 경로를 유지한다", () => {
  __ENV.CHAT_USERS = "bad";
  const output = handleSummary({ metrics: {} });
  assert.equal(JSON.parse(output["/runs/example/conditions.json"]).available, false);
  assert.ok(output["/runs/example/summary.json"]);
  delete __ENV.CHAT_RUN_DIR;
  assert.ok(handleSummary({ metrics: {} })["./out/last-summary.json"]);
});
