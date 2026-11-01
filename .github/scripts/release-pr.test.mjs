import assert from "node:assert/strict";
import test from "node:test";
import { AUTO_END, AUTO_START, latestReleaseDate, nextReleaseDate, renderReleaseBody } from "./release-pr.mjs";

test("직전 릴리스 날짜에 주기를 더하고 지난 회차는 건너뛴다", () => {
    const sundayNight = new Date("2026-11-08T12:00:00Z");
    assert.equal(nextReleaseDate({ lastReleaseDate: "2026-11-08", intervalDays: 7, now: sundayNight }), "2026-11-15");
    assert.equal(nextReleaseDate({ lastReleaseDate: "2026-11-29", intervalDays: 14, now: new Date("2026-11-30T01:00:00Z") }), "2026-12-13");
    assert.equal(nextReleaseDate({ lastReleaseDate: "2026-11-01", intervalDays: 7, now: new Date("2026-11-10T01:00:00Z") }), "2026-11-15");
});

test("날짜로 된 직전 릴리스가 없으면 오늘 이후의 일요일로 정한다", () => {
    assert.equal(nextReleaseDate({ lastReleaseDate: null, intervalDays: 14, now: new Date("2026-10-31T10:00:00Z") }), "2026-11-01");
    assert.equal(nextReleaseDate({ lastReleaseDate: null, intervalDays: 14, now: new Date("2026-11-01T12:00:00Z") }), "2026-11-08");
});

test("버전 제목은 무시하고 가장 늦은 날짜 제목을 고른다", () => {
    assert.equal(latestReleaseDate(["[Release] v2.4.24", "[Release] 2026-11-01", "[Release] 2026-11-08", ""]), "2026-11-08");
    assert.equal(latestReleaseDate(["[Release] v2.4.24"]), null);
});

test("본문이 없으면 자동 영역과 직접 쓰는 영역을 함께 만든다", () => {
    const body = renderReleaseBody("", ["[Feat] 차단 기능 (#1439)", "", "[Fix] 권한 조회 오류 (#1455)"]);
    assert.ok(body.startsWith(`${AUTO_START}\n## 포함된 PR`));
    assert.match(body, /- \[Feat\] 차단 기능 \(#1439\)\n- \[Fix\] 권한 조회 오류 \(#1455\)\n<!-- release-auto:end -->/);
    assert.match(body, /## 바뀌는 계약/);
});

test("자동 영역만 바꾸고 사람이 쓴 내용은 그대로 둔다", () => {
    const existing = `${AUTO_START}\n## 포함된 PR\n\n- 예전 항목\n${AUTO_END}\n\n## 바뀌는 계약\n\n- GET /api/v1/gisu/active 삭제 (담당자 확인 완료)\n`;
    const body = renderReleaseBody(existing, ["[Feat] 새 항목 (#1500)"]);
    assert.doesNotMatch(body, /예전 항목/);
    assert.match(body, /- \[Feat\] 새 항목 \(#1500\)/);
    assert.match(body, /- GET \/api\/v1\/gisu\/active 삭제 \(담당자 확인 완료\)\n$/);
});
