import { spawn } from "node:child_process";
import { openSync, closeSync, readFileSync, writeFileSync, renameSync } from "node:fs";
import { join } from "node:path";

export class SuiteError extends Error {
  constructor(message) { super(message); this.name = "SuiteError"; }
}

const running = new Set();
export const interruption = new AbortController();
export function interruptSuite() {
  interruption.abort(new SuiteError("사용자가 실행을 중단했습니다."));
  for (const process of running) signalProcess(process, "SIGTERM");
}

function signalProcess(process, signal) {
  if (!process.child.pid) return;
  try { globalThis.process.kill(-process.child.pid, signal); }
  catch (error) { if (!(error instanceof Error) || error.code !== "ESRCH") throw error; }
}

export function readJson(path) { return JSON.parse(readFileSync(path, "utf8")); }
export function saveJson(path, value) {
  writeFileSync(path + ".tmp", JSON.stringify(value, null, 2) + "\n");
  renameSync(path + ".tmp", path);
}

export function startProcess(command, args, options = {}) {
  interruption.signal.throwIfAborted();
  const descriptor = options.log ? openSync(options.log, "a", 0o600) : undefined;
  const child = spawn(command, args, { env: options.env || process.env,
    detached: true, stdio: ["ignore", descriptor ?? "pipe", descriptor ?? "pipe"] });
  let output = "";
  let error = "";
  if (descriptor !== undefined) closeSync(descriptor);
  else {
    child.stdout.on("data", (chunk) => { output += chunk; });
    child.stderr.on("data", (chunk) => { error += chunk; });
  }
  const result = { child, code: null, done: null };
  running.add(result);
  result.done = new Promise((resolve) => {
    child.on("error", (failure) => { result.code = 127; resolve({ code: 127, output: "", error: failure.message }); });
    child.on("close", (code) => {
      running.delete(result); result.code = code ?? 128; resolve({ code: result.code, output, error });
    });
  });
  return result;
}

export async function runProcess(command, args, options) {
  const result = await startProcess(command, args, options).done;
  if (result.code !== 0) throw new SuiteError(`${command} 종료 코드 ${result.code}. ${options?.log || "접속 설정을 확인하세요."}`);
  return result.output;
}

export async function stopProcess(process) {
  if (process.code !== null) return process.done;
  signalProcess(process, "SIGTERM");
  const timer = setTimeout(() => signalProcess(process, "SIGKILL"), 5000);
  try { return await process.done; } finally { clearTimeout(timer); }
}

export async function databaseState(env) {
  const sql = "SELECT json_build_object('threads',(SELECT count(*) FROM community_thread),"
    + "'members',(SELECT count(*) FROM chat_member),'messages',(SELECT count(*) FROM chat_message),"
    + "'reactions',(SELECT count(*) FROM chat_message_reaction),'reports',(SELECT count(*) FROM report),"
    + "'postgresVersion',current_setting('server_version'),'checkedAt',clock_timestamp())";
  const raw = await runProcess(env.PSQL_BIN || "psql", ["-X", "--no-password", "--dbname=" + env.PGDATABASE,
    "--quiet", "--no-align", "--tuples-only", "--set=ON_ERROR_STOP=1", "-c", sql], {
    env: { ...env, PGOPTIONS: (env.PGOPTIONS || "") + " -c default_transaction_read_only=on -c statement_timeout=30000" },
  });
  return JSON.parse(raw);
}

export async function observeRun(context) {
  const { env, directory, scripts } = context;
  const observer = startProcess("bash", [join(scripts, "watch-reaction-locks.sh")], {
    env: { ...env, LOCK_WATCH_SAMPLES: "999999" }, log: join(directory, "locks.jsonl") });
  const k6 = startProcess("bash", [join(scripts, "run-chat-test.sh"), env.CHAT_TEST_ID],
    { env, log: join(directory, "run.log") });
  try {
    const finished = await Promise.race([k6.done.then((result) => ({ kind: "load", ...result })),
      observer.done.then((result) => ({ kind: "observer", ...result }))]);
    if (finished.kind !== "load" || finished.code !== 0) {
      throw new SuiteError(finished.kind === "observer" ? "관측이 중단되어 실행을 멈췄습니다." : "부하 실행이 실패했습니다.");
    }
    if (observer.code !== null) throw new SuiteError("DB 관측 수집에 공백이 있습니다.");
  } finally {
    await Promise.all([k6, observer].map(stopProcess));
  }
}
