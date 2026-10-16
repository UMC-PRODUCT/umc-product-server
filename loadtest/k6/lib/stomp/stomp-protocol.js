const MAX_BUFFER_SIZE = 1024 * 1024;
const ESCAPES = { "\\": "\\", n: "\n", r: "\r", c: ":" };

export function utf8Length(value) {
  // STOMP의 content-length는 문자열 길이가 아니라 UTF-8 바이트 수다. 한글·이모지도 포함한다.
  return encodeURIComponent(value).replace(/%[0-9A-F]{2}|./g, "x").length;
}

function escapeHeader(value) {
  return String(value).replace(/\\/g, "\\\\").replace(/\r/g, "\\r")
    .replace(/\n/g, "\\n").replace(/:/g, "\\c");
}

function unescapeHeader(value) {
  return value.replace(/\\(.)?/g, (_, escaped) => {
    if (!Object.hasOwn(ESCAPES, escaped)) {
      throw new Error("잘못된 STOMP 헤더 이스케이프");
    }
    return ESCAPES[escaped];
  });
}

export function encodeFrame(command, headers = {}, body = "") {
  const escaped = command !== "CONNECT" && command !== "CONNECTED";
  const fields = { ...headers, "content-length": String(utf8Length(body)) };
  const lines = Object.entries(fields).map(([key, value]) =>
    escaped ? `${escapeHeader(key)}:${escapeHeader(value)}` : `${key}:${value}`,
  );
  return `${command}\n${lines.join("\n")}\n\n${body}\0`;
}

/**
 * 나뉘거나 합쳐져 들어오는 WebSocket 텍스트를 완성된 STOMP 프레임 배열로 변환한다.
 * 연결마다 하나씩 생성하고, 수신할 때마다 push(chunk)를 호출한다. 미완성 프레임은 다음 수신까지 보관한다.
 * heartbeat는 결과에서 제외하며, 잘못된 프레임이나 버퍼 제한 초과는 오류를 던진다.
 * StompClient가 내부에서 사용하므로 Chat 시나리오에서 직접 생성할 필요는 없다.
 */
export class StompDecoder {
  constructor() {
    this.buffer = "";
  }

  push(chunk) {
    this.buffer += chunk;
    if (this.buffer.length > MAX_BUFFER_SIZE) {
      throw new Error("STOMP 수신 버퍼 제한 초과");
    }
    const frames = [];
    while (true) {
      // heartbeat는 프레임이 아닌 빈 줄이다. 하나의 WebSocket 메시지에 여러 프레임이 올 수도 있다.
      this.buffer = this.buffer.replace(/^(?:\r?\n)+/, "");
      const separator = /\r?\n\r?\n/.exec(this.buffer);
      if (!separator) return frames;
      const lines = this.buffer.slice(0, separator.index).split(/\r?\n/);
      const command = lines.shift();
      const headers = Object.create(null);
      for (const line of lines) {
        const colon = line.indexOf(":");
        if (colon <= 0) throw new Error("잘못된 STOMP 헤더");
        const decode = command === "CONNECTED" ? (value) => value : unescapeHeader;
        const key = decode(line.slice(0, colon));
        if (!Object.hasOwn(headers, key)) headers[key] = decode(line.slice(colon + 1));
      }
      const bodyStart = separator.index + separator[0].length;
      let bodyEnd;
      if (Object.hasOwn(headers, "content-length")) {
        if (!/^\d+$/.test(headers["content-length"])) throw new Error("잘못된 content-length");
        const length = Number(headers["content-length"]);
        if (!Number.isSafeInteger(length) || length > MAX_BUFFER_SIZE) {
          throw new Error("STOMP 본문 길이 제한 초과");
        }
        bodyEnd = bodyStart;
        let bytes = 0;
        // 본문의 NUL도 데이터일 수 있으므로 content-length가 있으면 바이트 수로 경계를 찾는다.
        for (const character of this.buffer.slice(bodyStart)) {
          if (bytes === length) break;
          bytes += utf8Length(character);
          bodyEnd += character.length;
          if (bytes > length) throw new Error("content-length가 UTF-8 문자 중간을 가리킵니다");
        }
        if (bytes < length || bodyEnd === this.buffer.length) return frames;
        if (this.buffer[bodyEnd] !== "\0") throw new Error("STOMP 종료 문자가 없습니다");
      } else {
        bodyEnd = this.buffer.indexOf("\0", bodyStart);
        if (bodyEnd < 0) return frames;
      }
      frames.push({ command, headers, body: this.buffer.slice(bodyStart, bodyEnd) });
      this.buffer = this.buffer.slice(bodyEnd + 1);
    }
  }
}
