import assert from "node:assert/strict";
import test from "node:test";

import { encodeFrame, StompDecoder, utf8Length } from "../lib/stomp/stomp-protocol.js";

test("한글과 이모지 본문의 UTF-8 바이트 길이를 기록한다", () => {
  assert.equal(utf8Length("한글😀"), 10);
  const frame = encodeFrame("SEND", { destination: "/app/test" }, "한글😀");
  assert.match(frame, /content-length:10\n/);
  assert.equal(new StompDecoder().push(frame)[0].body, "한글😀");
});

test("나뉘어 도착한 프레임과 heartbeat·연속 프레임을 처리한다", () => {
  const decoder = new StompDecoder();
  const first = encodeFrame("MESSAGE", {}, "준비");
  const second = encodeFrame("MESSAGE", {}, "완료");
  assert.deepEqual(decoder.push("\n\r\n" + first.slice(0, 12)), []);
  const frames = decoder.push(first.slice(12) + "\n" + second);
  assert.deepEqual(frames.map((frame) => frame.body), ["준비", "완료"]);
});

test("content-length가 있으면 본문 속 NUL을 종료 문자로 오인하지 않는다", () => {
  const body = "a\0나";
  assert.equal(new StompDecoder().push(encodeFrame("MESSAGE", {}, body))[0].body, body);
});

test("헤더의 콜론·개행·역슬래시를 이스케이프하고 복원한다", () => {
  const value = "a:b\nc\\d\r";
  const frame = encodeFrame("MESSAGE", { custom: value });
  assert.equal(new StompDecoder().push(frame)[0].headers.custom, value);
  assert.match(encodeFrame("CONNECT", { host: "localhost:8080" }), /host:localhost:8080/);
});

test("CRLF와 content-length 없는 프레임, 중복 헤더의 첫 값을 처리한다", () => {
  const [frame] = new StompDecoder().push("MESSAGE\r\nx:1\r\nx:2\r\n\r\n{}\0");
  assert.equal(frame.headers.x, "1");
  assert.equal(frame.body, "{}");
});

test("본문 종료 문자가 나중에 오면 프레임을 보류한다", () => {
  const decoder = new StompDecoder();
  assert.deepEqual(decoder.push("MESSAGE\ncontent-length:3\n\n한"), []);
  assert.equal(decoder.push("\0")[0].body, "한");
});

test("잘못된 바이트 길이·종료 문자·이스케이프를 거부한다", () => {
  for (const frame of ["MESSAGE\ncontent-length:2\n\n한\0", "MESSAGE\ncontent-length:0\n\nx",
    "MESSAGE\ncontent-length:-1\n\n\0", "MESSAGE\nx:bad\\t\n\n\0"]) {
    assert.throws(() => new StompDecoder().push(frame));
  }
});

test("비정상적으로 큰 수신 버퍼와 본문 길이를 거부한다", () => {
  assert.throws(() => new StompDecoder().push("x".repeat(1024 * 1024 + 1)));
  assert.throws(() => new StompDecoder().push("MESSAGE\ncontent-length:1048577\n\n"));
});
