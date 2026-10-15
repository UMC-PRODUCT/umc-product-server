export function positiveInteger(value, fallback, name, min, max) {
  const raw = value === undefined ? String(fallback) : String(value);
  const number = Number(raw);
  if (!/^[1-9][0-9]*$/.test(raw) || !Number.isSafeInteger(number) || number < min || number > max) {
    throw new Error(`${name}는 ${min}~${max} 사이 정수여야 합니다`);
  }
  return number;
}

export function durationMs(value) {
  const units = { ms: 1, s: 1000, m: 60000, h: 3600000 };
  const parts = [...String(value).matchAll(/([0-9]+(?:\.[0-9]+)?)(ms|s|m|h)/g)];
  const total = parts.reduce((sum, part) => sum + Number(part[1]) * units[part[2]], 0);
  if (parts.map((part) => part[0]).join("") !== value || !Number.isSafeInteger(total)
      || total < 1 || total > 86400000) {
    throw new Error("DURATION은 1ms~24h 사이여야 합니다. 예: 30s, 1m30s");
  }
  return total;
}

export function readChatConfig(env) {
  const baseUrl = (env.BASE_URL || "http://localhost:8080").replace(/\/+$/, "");
  if (!/^https?:\/\/[^/?#\s]+$/.test(baseUrl)) throw new Error("BASE_URL에는 앱의 http(s) 주소만 지정하세요");
  const timeoutMs = positiveInteger(env.CHAT_TIMEOUT_MS, 10000, "CHAT_TIMEOUT_MS", 100, 60000);
  return { baseUrl, timeoutMs };
}
