import assert from "node:assert/strict";
import { registerHooks } from "node:module";
import test from "node:test";

globalThis.__ENV = { PROFILE: "load", SCENARIO: "chat-custom", CHAT_RUN_DIR: "/runs/example", CUSTOM_BATCH: "5" };
globalThis.open = () => JSON.stringify({ baseUrl: "http://localhost:8080", memberIds: ["1", "2"],
  rooms: [{ threadId: "10", memberIds: ["1", "2"] }] });
registerHooks({
  resolve(specifier, context, next) {
    if (specifier.startsWith("https://")) return { url: "mock:summary", shortCircuit: true };
    if (context.parentURL?.includes("/script.js") && specifier.startsWith("./scenarios/")) {
      return { url: "mock:scenario", shortCircuit: true };
    }
    return next(specifier, context);
  },
  load(url, context, next) {
    if (url.startsWith("file:") && new URL(url).pathname.endsWith("/script.js")) {
      const result = next(url, context);
      return { ...result, source: String(result.source).replace("  load: {", `  load: {
        "chat-custom": { fn: loadChatMessage, requiresSeed: true, getOptions: loadChatMessageOptions,
          validateSeed: validateLoadChatSeed, prepareRun: prepareChatRun, getRunConditions: loadChatConditions },`) };
    }
    if (url === "mock:summary") return { format: "module", shortCircuit: true,
      source: 'export const textSummary = () => "요약";' };
    if (url === "mock:scenario") return { format: "module", shortCircuit: true, source: `
      import { readLoadConfig } from "${new URL("../config/chat-load.js", import.meta.url).href}";
      export default function() {}
      export const requiresSeed = true;
      export const getOptions = () => ({ scenarios: {
        chat_load: { executor: "per-vu-iterations", vus: 2, iterations: 1 }
      } });
      export function validateSeed() {}
      export function prepareRun() {}
      export function getRunConditions(seed) {
        return { config: readLoadConfig(__ENV), vuMemberIds: seed.memberIds.map(id => [id]),
          customMeasurement: { batchSize: Number(__ENV.CUSTOM_BATCH) } };
      }
    ` };
    return next(url, context);
  },
});
const { handleSummary, options } = await import("../script.js");

test("새 시나리오를 등록하면 공통 저장 코드 수정 없이 추가 설정과 최초 실행 옵션을 보존한다", () => {
  options.scenarios = {};
  const output = handleSummary({ options: { noColor: true }, metrics: {} });
  const conditions = JSON.parse(output["/runs/example/conditions.json"]);
  assert.equal(conditions.k6Options.scenarios.chat_load.vus, 2);
  assert.deepEqual(conditions.vuMemberIds, [["1"], ["2"]]);
  assert.equal(conditions.scenario, "chat-custom");
  assert.equal(conditions.config.tags.scenario, "chat-custom");
  assert.equal(conditions.customMeasurement.batchSize, 5);
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

test("기록 함수가 없는 조합은 부하 전에 거부하고 기존 HTTP 직접 실행은 유지한다", async () => {
  Object.assign(__ENV, { PROFILE: "smoke", SCENARIO: "health-check", CHAT_RUN_DIR: "/runs/example" });
  await assert.rejects(import("../script.js?unsupported-recording"), /getRunConditions/);
  delete __ENV.CHAT_RUN_DIR;
  const http = await import("../script.js?http-direct");
  assert.ok(http.handleSummary({ metrics: {} })["./out/last-summary.json"]);
});
