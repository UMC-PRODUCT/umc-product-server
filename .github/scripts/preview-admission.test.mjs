import assert from "node:assert/strict";
import test from "node:test";
import { ADMISSION_COMMENT_MARKER, checkMigrationLag, decideAdmission } from "./preview-admission.mjs";

const base = [
    "AGENTS.md",
    "V2026.02.25.01.30__init_schema.sql",
    "V2026.10.31.00.00__create_report_action.sql",
    "V2026.11.01.13.00__create_report_reason.sql",
];

const admission = (patch = {}) => decideAdmission({
    prNumber: 42,
    otherPreviewPrNumbers: [],
    baseFiles: base,
    headFiles: base,
    ...patch,
});

test("자리가 있고 develop 마이그레이션을 모두 가진 PR은 받아들이고 주소를 안내한다", () => {
    const decision = admission({ otherPreviewPrNumbers: [7], headFiles: [...base, "V2026.11.02.09.00__add_column.sql"] });
    assert.equal(decision.admitted, true);
    assert.ok(decision.comment.startsWith(ADMISSION_COMMENT_MARKER));
    assert.match(decision.comment, /api-pr-42\.university\.neordinary\.com/);
});

test("다른 preview가 2개면 마이그레이션과 무관하게 거절하고 사용 중인 PR을 알려준다", () => {
    const decision = admission({ otherPreviewPrNumbers: [31, 7], headFiles: [] });
    assert.equal(decision.admitted, false);
    assert.match(decision.comment, /#7, #31/);
    assert.doesNotMatch(decision.comment, /마이그레이션/);
});

test("develop에만 있는 마이그레이션이 있으면 거절하고 파일을 나열한다", () => {
    const decision = admission({ headFiles: base.slice(0, 3) });
    assert.equal(decision.admitted, false);
    assert.match(decision.comment, /V2026\.11\.01\.13\.00__create_report_reason\.sql/);
});

test("develop 최신보다 낮은 새 마이그레이션은 out-of-order로 막히므로 거절한다", () => {
    const lag = checkMigrationLag(base, [...base, "V2026.11.01.12.59__add_late.sql"]);
    assert.deepEqual(lag, { missing: [], outdated: ["V2026.11.01.12.59__add_late.sql"] });
    assert.equal(admission({ headFiles: [...base, "V2026.11.01.12.59__add_late.sql"] }).admitted, false);
});

test("버전은 문자열이 아니라 구간별 숫자로 비교한다", () => {
    const lag = checkMigrationLag(["V2026.9.1.0.0__a.sql"], ["V2026.9.1.0.0__a.sql", "V2026.10.1.0.0__b.sql"]);
    assert.deepEqual(lag, { missing: [], outdated: [] });
});

test("마이그레이션 형식이 아닌 파일은 비교에서 제외한다", () => {
    assert.deepEqual(checkMigrationLag(base, base.slice(1)), { missing: [], outdated: [] });
});

test("빠진 파일이 많으면 10개까지만 나열한다", () => {
    const many = Array.from({ length: 12 }, (_, index) => `V2026.11.${String(index + 1).padStart(2, "0")}.00.00__m.sql`);
    const decision = admission({ baseFiles: many, headFiles: [] });
    assert.match(decision.comment, /외 2개/);
});
