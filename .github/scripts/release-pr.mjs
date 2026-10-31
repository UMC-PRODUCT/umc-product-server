import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { addDays, parseDate, releaseDateFromTitle, upcomingSunday } from "./deprecation-report.mjs";

export const AUTO_START = "<!-- release-auto:start -->";
export const AUTO_END = "<!-- release-auto:end -->";

const MANUAL_SECTION = [
    "## 바뀌는 계약",
    "",
    "<!--",
    "이번 릴리스에서 새로 지원 중단하거나 지우는 계약을 적어 주세요.",
    "지원 중단 목록과 삭제 예정 항목은 이 PR의 '지원 중단 항목 점검' 댓글에 자동으로 나옵니다.",
    "-->",
].join("\n");

function koreanToday(now) {
    return new Date(now.getTime() + 9 * 3_600_000).toISOString().slice(0, 10);
}

// 직전 릴리스 날짜에 주기를 더한다. 건너뛴 회차가 있으면 오늘 이후가 될 때까지 주기를 더한다.
export function nextReleaseDate({ lastReleaseDate, intervalDays, now = new Date() }) {
    const today = koreanToday(now);
    if (!lastReleaseDate) {
        const sunday = upcomingSunday(now);
        return sunday === today ? addDays(sunday, 7) : sunday;
    }
    let candidate = addDays(lastReleaseDate, intervalDays);
    while (parseDate(candidate) < parseDate(today)) {
        candidate = addDays(candidate, intervalDays);
    }
    return candidate;
}

export function latestReleaseDate(titles) {
    return titles
        .map((title) => releaseDateFromTitle(title))
        .filter(Boolean)
        .sort()
        .at(-1) ?? null;
}

export function renderReleaseBody(existingBody, commitSubjects) {
    const entries = commitSubjects.map((subject) => subject.trim()).filter(Boolean).map((subject) => `- ${subject}`);
    const auto = [
        AUTO_START,
        "## 포함된 PR",
        "",
        "이 목록은 `develop`에 합쳐질 때마다 자동으로 갱신됩니다.",
        "",
        ...(entries.length ? entries : ["- 없음"]),
        AUTO_END,
    ].join("\n");
    const body = existingBody ?? "";
    const start = body.indexOf(AUTO_START);
    const end = body.indexOf(AUTO_END);
    if (start === -1 || end === -1 || end < start) {
        return `${auto}\n\n${MANUAL_SECTION}\n`;
    }
    return `${body.slice(0, start)}${auto}${body.slice(end + AUTO_END.length)}`;
}

function main() {
    const [command, ...rest] = process.argv.slice(2);
    if (command === "title") {
        const [intervalText, titlesFile] = rest;
        const intervalDays = Number(intervalText);
        if (!Number.isInteger(intervalDays) || intervalDays < 1 || !titlesFile) {
            throw new Error("usage: release-pr.mjs title <interval-days> <merged-release-titles-file>");
        }
        const titles = readFileSync(titlesFile, "utf8").split("\n");
        process.stdout.write(`[Release] ${nextReleaseDate({ lastReleaseDate: latestReleaseDate(titles), intervalDays })}\n`);
        return;
    }
    if (command === "body") {
        const [existingBodyFile, subjectsFile] = rest;
        if (!existingBodyFile || !subjectsFile) {
            throw new Error("usage: release-pr.mjs body <existing-body-file> <commit-subjects-file>");
        }
        process.stdout.write(renderReleaseBody(readFileSync(existingBodyFile, "utf8"), readFileSync(subjectsFile, "utf8").split("\n")));
        return;
    }
    throw new Error("usage: release-pr.mjs <title|body> ...");
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    main();
}
