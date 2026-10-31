import assert from "node:assert/strict";
import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";
import {
    addDays,
    classify,
    releaseDateFromTitle,
    renderReport,
    REPORT_MARKER,
    scanRepository,
    scanSource,
    upcomingSunday,
} from "./deprecation-report.mjs";

const controller = `
public class ChallengerSearchController {

    @Deprecated(since = "2026-11-01", forRemoval = true)
    @Operation(
        summary = "챌린저 검색",
        deprecated = true,
        description = """
            지원 중단: \`GET /api/v1/member/search\`. 삭제 예정: 2026-11-29 릴리스, 담당자: github-id
            """
    )
    @GetMapping("search/global")
    public ChallengerSearchResponse searchGlobal() {
        return null;
    }

    @Deprecated(since = "v2.0.0", forRemoval = true)
    @GetMapping("/active")
    public ActiveGisuResponse getActiveGisu() {
        return null;
    }
}
`;

test("설명의 삭제 예정·담당자·대체를 읽고 표기가 없는 @Deprecated는 따로 모은다", () => {
    const { scheduled, unscheduled } = scanSource("A.java", controller);
    assert.deepEqual(scheduled, [{
        path: "A.java",
        line: 4,
        declaration: "public ChallengerSearchResponse searchGlobal() {",
        removalDate: "2026-11-29",
        owner: "github-id",
        replacement: "`GET /api/v1/member/search`",
    }]);
    assert.deepEqual(unscheduled, [{ path: "A.java", line: 17, declaration: "public ActiveGisuResponse getActiveGisu() {" }]);
});

test("Javadoc, record 컴포넌트, enum 상수의 표기를 읽고 주석 처리된 @Deprecated는 무시한다", () => {
    const source = `
public enum ExampleStatus {
    ACTIVE,

    /**
     * @deprecated 지원 중단: {@link #ACTIVE}. 삭제 예정: 2026-12-13 릴리스, 담당자: owner-b
     */
    @Deprecated(since = "2026-11-01", forRemoval = true)
    LEGACY_ACTIVE
}

public record ProjectSummaryResponse(
    Long projectId,

    @Deprecated(since = "2026-11-01", forRemoval = true)
    @Schema(
        deprecated = true,
        description = "지원 중단: \`ownerMemberId\`. 삭제 예정: 2026-11-29 릴리스"
    )
    Long ownerId,

    Long ownerMemberId
) {
}

//    @Deprecated(since = "v1.2.5", forRemoval = true)
//    @GetMapping("member/me")
`;
    const { scheduled, unscheduled } = scanSource("B.java", source);
    assert.deepEqual(scheduled.map(({ declaration, removalDate, owner }) => ({ declaration, removalDate, owner })), [
        { declaration: "LEGACY_ACTIVE", removalDate: "2026-12-13", owner: "owner-b" },
        { declaration: "Long ownerId,", removalDate: "2026-11-29", owner: null },
    ]);
    assert.deepEqual(unscheduled, []);
});

test("릴리스 날짜 기준으로 지울 항목과 다음 릴리스 예정 항목을 나눈다", () => {
    const items = ["2026-11-22", "2026-11-29", "2026-12-06", "2026-12-13", "2026-12-20"].map((removalDate, index) => ({
        path: `${index}.java`, line: 1, declaration: "x", removalDate, owner: index === 1 ? null : "owner", replacement: null,
    }));
    const result = classify(items, "2026-11-29", 14);
    assert.deepEqual(result.due.map((item) => item.removalDate), ["2026-11-22", "2026-11-29"]);
    assert.deepEqual(result.upcoming.map((item) => item.removalDate), ["2026-12-06", "2026-12-13"]);
    assert.deepEqual(result.missingOwner.map((item) => item.removalDate), ["2026-11-29"]);
});

test("릴리스 PR 제목에서 날짜를 읽고 없는 날짜는 거부한다", () => {
    assert.equal(releaseDateFromTitle("[Release] 2026-11-08"), "2026-11-08");
    assert.equal(releaseDateFromTitle("[Release] v2.4.24"), null);
    assert.throws(() => releaseDateFromTitle("[Release] 2026-02-30"), /does not exist/);
    assert.equal(addDays("2026-11-29", 14), "2026-12-13");
});

test("한국 시간 기준으로 오늘을 포함한 가장 가까운 일요일을 고른다", () => {
    assert.equal(upcomingSunday(new Date("2026-10-31T10:00:00Z")), "2026-11-01");
    assert.equal(upcomingSunday(new Date("2026-10-31T16:00:00Z")), "2026-11-01");
    assert.equal(upcomingSunday(new Date("2026-11-01T15:30:00Z")), "2026-11-08");
});

test("리포트에 담당자 멘션과 위치 링크를 넣는다", () => {
    const item = {
        path: "monolith/src/main/java/A.java", line: 4, declaration: "public A a() {", removalDate: "2026-11-29",
        owner: "github-id", replacement: "`GET /b`",
    };
    const report = renderReport({
        releaseDate: "2026-11-29", intervalDays: 14, due: [item], upcoming: [], missingOwner: [], unscheduled: [],
    }, { repository: "UMC-PRODUCT/umc-product-server", sha: "abc123" });
    assert.ok(report.startsWith(REPORT_MARKER));
    assert.match(report, /\| 2026-11-29 \| @github-id \| \[monolith\/src\/main\/java\/A\.java:4\]\(https:\/\/github\.com\/UMC-PRODUCT\/umc-product-server\/blob\/abc123\/monolith\/src\/main\/java\/A\.java#L4\)/);
    assert.match(report, /### 다음 릴리스에서 지울 예정인 항목 \(0\)\n\n없습니다\./);
});

test("src/main/java 아래의 Java 파일만 훑는다", (t) => {
    const root = mkdtempSync(join(tmpdir(), "umc-deprecation-report-test-"));
    t.after(() => rmSync(root, { recursive: true, force: true }));
    mkdirSync(join(root, "monolith/src/main/java"), { recursive: true });
    mkdirSync(join(root, "monolith/src/test/java"), { recursive: true });
    writeFileSync(join(root, "monolith/src/main/java/A.java"), controller);
    writeFileSync(join(root, "monolith/src/test/java/ATest.java"), controller);
    const { scheduled, unscheduled } = scanRepository(root);
    assert.deepEqual(scheduled.map((item) => item.path), ["monolith/src/main/java/A.java"]);
    assert.deepEqual(unscheduled.map((item) => item.path), ["monolith/src/main/java/A.java"]);
});
