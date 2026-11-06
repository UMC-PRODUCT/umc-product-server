import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";

import {
    buildBodyOnlyReview,
    buildReview,
    extractResult,
    firstLinesAt,
    neutralizeMentions,
    parseDiffLines,
} from "./post-ai-review.mjs";

const PATCH = [
    "diff --git a/src/A.java b/src/A.java",
    "index 1..2 100644",
    "--- a/src/A.java",
    "+++ b/src/A.java",
    "@@ -10,3 +10,4 @@ class A {",
    " keep",
    "-old",
    "+new one",
    "+new two",
    " keep",
    "diff --git a/old.txt b/old.txt",
    "deleted file mode 100644",
    "--- a/old.txt",
    "+++ /dev/null",
    "@@ -1 +0,0 @@",
    "-gone",
    "",
].join("\n");

const finding = (overrides = {}) => ({
    priority: "P3",
    rule: "ARCH-05",
    title: "다른 도메인 저장소를 직접 씁니다",
    path: "src/A.java",
    start_line: null,
    line: 11,
    problem: "이 줄이 저장소를 직접 주입합니다.",
    when: "도메인을 나눌 때 깨집니다.",
    fix: "UseCase를 주입합니다.",
    suggestion: null,
    also_by_other_reviewer: false,
    ...overrides,
});

const reviewer = (findings) => ({ reviewer: "bug", findings, checked_ok: [], excluded: [] });

const context = {
    repository: "UMC-PRODUCT/umc-product-server",
    head: "abc123",
    diffLines: parseDiffLines(PATCH),
};

test("diff의 추가 줄과 맥락 줄만 코멘트 가능한 줄로 본다", () => {
    const lines = parseDiffLines(PATCH);

    assert.deepEqual([...lines.get("src/A.java")], [10, 11, 12, 13]);
    assert.equal(lines.has("old.txt"), false);
});

test("git이 따옴표로 감싼 한글 경로도 코멘트 가능한 파일로 본다", () => {
    const quoted = [
        'diff --git "a/docs/\\352\\270\\200.md" "b/docs/\\352\\270\\200.md"',
        '--- "a/docs/\\352\\270\\200.md"',
        '+++ "b/docs/\\352\\270\\200.md"',
        "@@ -1 +1 @@",
        "-old",
        "+new",
    ].join("\n");

    assert.deepEqual([...parseDiffLines(quoted).get("docs/글.md")], [1]);
});

test("실제 diff의 한글 파일 지적을 줄 코멘트로 보낸다", () => {
    const review = buildReview(reviewer([finding({ path: "docs/글.md", line: 1 })]), {
        ...context,
        diffLines: parseDiffLines('+++ "b/docs/\\352\\270\\200.md"\n@@ -1 +1 @@\n+new'),
    });

    assert.equal(review.comments.length, 1);
    assert.equal(review.comments[0].path, "docs/글.md");
});

test("바뀐 줄의 지적은 그 줄에 달고 문제, 시점, 수정을 모두 적는다", () => {
    const review = buildReview(reviewer([finding()]), context);

    assert.equal(review.comments.length, 1);
    assert.equal(review.comments[0].path, "src/A.java");
    assert.equal(review.comments[0].line, 11);
    assert.equal(review.comments[0].side, "RIGHT");
    assert.match(review.comments[0].body, /^\*\*\[P3\]\[ARCH-05\] 다른 도메인 저장소를 직접 씁니다\*\*/);
    for (const part of ["이 줄이 저장소를", "도메인을 나눌 때", "UseCase를 주입"]) {
        assert.ok(review.comments[0].body.includes(part), part);
    }
    assert.equal(review.body.includes("다른 도메인 저장소"), false);
});

test("여러 줄 지적은 범위를 보내고 suggestion을 붙인다", () => {
    const review = buildReview(reviewer([finding({ start_line: 11, line: 12, suggestion: "fixed();" })]), context);

    assert.equal(review.comments[0].start_line, 11);
    assert.equal(review.comments[0].start_side, "RIGHT");
    assert.match(review.comments[0].body, /```suggestion\nfixed\(\);\n```/);
});

test("바뀐 줄 밖의 지적은 커밋을 고정한 링크와 함께 본문에 적는다", () => {
    const review = buildReview(reviewer([finding({ path: "docs/가이드.md", start_line: 42, line: 45, suggestion: "x" })]), context);

    assert.deepEqual(review.comments, []);
    assert.ok(review.body.includes(
        "https://github.com/UMC-PRODUCT/umc-product-server/blob/abc123/docs/%EA%B0%80%EC%9D%B4%EB%93%9C.md#L42-L45",
    ));
    assert.equal(review.body.includes("```suggestion"), false);
});

test("범위 일부가 diff 밖이면 줄 코멘트로 보내지 않는다", () => {
    const review = buildReview(reviewer([finding({ start_line: 9, line: 11 })]), context);

    assert.deepEqual(review.comments, []);
});

test("본문 첫 줄은 리뷰어 이름이고 표에 우선순위별 개수를 적는다", () => {
    const review = buildReview(reviewer([finding(), finding({ priority: "P1" })]), context);
    const [first] = review.body.split("\n");

    assert.equal(first, "🐛 버그·보안 리뷰어");
    assert.ok(review.body.includes("| 1 | 0 | 1 | 0 | 0 |"));
    assert.equal(review.event, "COMMENT");
    assert.equal(review.commit_id, "abc123");
});

test("지적이 없어도 리뷰를 만들고 확인한 것과 뺀 지적은 접어 둔다", () => {
    const review = buildReview(
        { reviewer: "quality", findings: [], checked_ok: ["경로 확인"], excluded: [{ title: "오탐", reason: "줄이 없음" }] },
        context,
    );

    assert.ok(review.body.startsWith("🧹 코드 품질 리뷰어"));
    assert.ok(review.body.includes("<summary>문제없다고 확인한 것 1건</summary>"));
    assert.ok(review.body.includes("- 오탐: 줄이 없음"));
});

test("422 대비판은 줄 코멘트 없이 지적을 모두 본문에 남긴다", () => {
    const review = buildBodyOnlyReview(reviewer([finding()]), context);

    assert.deepEqual(review.comments, []);
    assert.ok(review.body.includes("다른 도메인 저장소를 직접 씁니다"));
    assert.ok(review.body.includes("/blob/abc123/src/A.java#L11"));
});

test("코드 밖의 @ 글자만 알림이 가지 않게 바꾼다", () => {
    assert.equal(neutralizeMentions("@Transactional 위치"), "@​Transactional 위치");
    assert.equal(neutralizeMentions("`@Transactional` 위치"), "`@Transactional` 위치");
    assert.equal(neutralizeMentions("메일 a@b"), "메일 a@​b");
});

test("실행 기록의 마지막 result에서 구조화 결과를 꺼낸다", () => {
    const raw = JSON.stringify([
        { type: "system" },
        { type: "result", structured_output: { reviewers: [reviewer([])] } },
    ]);

    assert.equal(extractResult(raw).reviewers[0].reviewer, "bug");
});

test("구조화 결과가 없거나 리뷰어가 틀리면 조용히 넘어가지 않는다", () => {
    assert.throws(() => extractResult(JSON.stringify([{ type: "result" }])), /reviewers/);
    assert.throws(() => extractResult(JSON.stringify({ reviewers: [{ reviewer: "other" }] })), /other/);
});

test("같은 커밋의 봇 리뷰만 이미 남긴 리뷰로 센다", () => {
    const reviews = [
        { commit_id: "abc123", user: { login: "github-actions[bot]" }, body: "🐛 버그·보안 리뷰어\n\n표" },
        { commit_id: "abc123", user: { login: "someone" }, body: "🧹 코드 품질 리뷰어" },
        { commit_id: "old", user: { login: "github-actions[bot]" }, body: "🧹 코드 품질 리뷰어" },
    ];

    assert.deepEqual(firstLinesAt(reviews, "abc123"), ["🐛 버그·보안 리뷰어"]);
});

test("CI와 저장소 설정의 pstack 버전이 같다", () => {
    const read = (path) => readFileSync(new URL(`../../${path}`, import.meta.url), "utf8");
    const workflowVersion = /PSTACK_SHA: \w+ # (v[\d.]+)/.exec(read(".github/workflows/ai-review.yml"))[1];
    const settings = JSON.parse(read(".claude/settings.json"));

    assert.equal(workflowVersion, settings.extraKnownMarketplaces["pstack-claude"].source.ref);
});
