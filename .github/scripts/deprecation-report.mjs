import { readdirSync, readFileSync } from "node:fs";
import { join, relative, resolve, sep } from "node:path";
import { fileURLToPath } from "node:url";

// docs/guides/API_호환성_정책.md 5절 2단계의 표기 형식을 읽는다.
// 예: 지원 중단: `GET /api/v1/member/search`. 삭제 예정: 2026-11-29 릴리스, 담당자: github-id
const SCHEDULE_PATTERN = /삭제 예정:\s*(\d{4}-\d{2}-\d{2})\s*릴리스(?:\s*,\s*담당자:\s*([A-Za-z0-9][A-Za-z0-9-]{0,38}))?/;
const REPLACEMENT_PATTERN = /지원 중단:\s*(.+?)\.\s*삭제 예정/;
const DEPRECATED_ANNOTATION = /@Deprecated\b/;
const SKIPPED_DIRECTORIES = new Set([".git", ".gradle", "build", "node_modules", "out"]);
export const REPORT_MARKER = "<!-- deprecation-report -->";

export function parseDate(value) {
    if (typeof value !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        throw new Error(`Date must be YYYY-MM-DD: ${value}`);
    }
    const [year, month, day] = value.split("-").map(Number);
    const time = Date.UTC(year, month - 1, day);
    if (new Date(time).toISOString().slice(0, 10) !== value) {
        throw new Error(`Date does not exist: ${value}`);
    }
    return time;
}

export function addDays(value, days) {
    return new Date(parseDate(value) + days * 86_400_000).toISOString().slice(0, 10);
}

export function releaseDateFromTitle(title) {
    const match = /\[Release\]\s*(\d{4}-\d{2}-\d{2})/.exec(title ?? "");
    if (!match) {
        return null;
    }
    parseDate(match[1]);
    return match[1];
}

// 오늘을 포함한 가장 가까운 일요일(Asia/Seoul 기준)
export function upcomingSunday(now = new Date()) {
    const today = new Date(now.getTime() + 9 * 3_600_000).toISOString().slice(0, 10);
    const weekday = new Date(parseDate(today)).getUTCDay();
    return addDays(today, (7 - weekday) % 7);
}

function startOfAnnotationBlock(lines, index) {
    let start = index;
    while (start > 0) {
        const previous = lines[start - 1].trim();
        if (previous === "" || previous === "}" || previous.endsWith(";") || previous.endsWith("{")) {
            break;
        }
        start -= 1;
    }
    return start;
}

function parenthesisDelta(line) {
    let delta = 0;
    for (const character of line) {
        if (character === "(") {
            delta += 1;
        } else if (character === ")") {
            delta -= 1;
        }
    }
    return delta;
}

// @Deprecated 아래의 어노테이션 인자(설명 텍스트 블록 포함)를 지나 선언부 줄을 찾는다.
function declarationIndex(lines, index) {
    let depth = 0;
    for (let current = index; current < lines.length; current += 1) {
        const trimmed = lines[current].trim();
        if (depth === 0 && current > index && !trimmed.startsWith("@") && trimmed !== "") {
            return current;
        }
        depth += parenthesisDelta(trimmed);
    }
    return lines.length - 1;
}

export function scanSource(path, text) {
    const lines = text.split(/\r?\n/);
    const scheduled = [];
    const unscheduled = [];
    lines.forEach((line, index) => {
        const trimmed = line.trim();
        if (trimmed.startsWith("//") || !DEPRECATED_ANNOTATION.test(trimmed)) {
            return;
        }
        const declaration = declarationIndex(lines, index);
        const block = lines.slice(startOfAnnotationBlock(lines, index), declaration + 1);
        const item = {
            path,
            line: index + 1,
            declaration: lines[declaration].trim().slice(0, 100),
        };
        const scheduleLine = block.find((candidate) => SCHEDULE_PATTERN.test(candidate));
        if (!scheduleLine) {
            unscheduled.push(item);
            return;
        }
        const [, removalDate, owner] = SCHEDULE_PATTERN.exec(scheduleLine);
        parseDate(removalDate);
        scheduled.push({
            ...item,
            removalDate,
            owner: owner ?? null,
            replacement: REPLACEMENT_PATTERN.exec(scheduleLine)?.[1]?.trim() ?? null,
        });
    });
    return { scheduled, unscheduled };
}

function javaSourceFiles(directory, files = []) {
    for (const entry of readdirSync(directory, { withFileTypes: true })) {
        if (entry.isSymbolicLink() || SKIPPED_DIRECTORIES.has(entry.name)) {
            continue;
        }
        const path = join(directory, entry.name);
        if (entry.isDirectory()) {
            javaSourceFiles(path, files);
        } else if (entry.isFile() && entry.name.endsWith(".java") && path.split(sep).join("/").includes("/src/main/java/")) {
            files.push(path);
        }
    }
    return files;
}

export function scanRepository(root) {
    const scheduled = [];
    const unscheduled = [];
    for (const file of javaSourceFiles(resolve(root)).sort()) {
        const path = relative(resolve(root), file).split(sep).join("/");
        const result = scanSource(path, readFileSync(file, "utf8"));
        scheduled.push(...result.scheduled);
        unscheduled.push(...result.unscheduled);
    }
    return { scheduled, unscheduled };
}

export function classify(scheduled, releaseDate, intervalDays) {
    const release = parseDate(releaseDate);
    const nextRelease = parseDate(addDays(releaseDate, intervalDays));
    const byDate = (left, right) => left.removalDate.localeCompare(right.removalDate) || left.path.localeCompare(right.path);
    return {
        all: [...scheduled].sort(byDate),
        due: scheduled.filter((item) => parseDate(item.removalDate) <= release).sort(byDate),
        upcoming: scheduled
            .filter((item) => parseDate(item.removalDate) > release && parseDate(item.removalDate) <= nextRelease)
            .sort(byDate),
        missingOwner: scheduled.filter((item) => !item.owner).sort(byDate),
    };
}

function location(item, links) {
    const label = `${item.path}:${item.line}`;
    if (!links) {
        return `\`${label}\``;
    }
    return `[${label}](https://github.com/${links.repository}/blob/${links.sha}/${item.path}#L${item.line})`;
}

function scheduledRows(items, links) {
    const rows = items.map((item) => {
        const owner = item.owner ? `@${item.owner}` : "미기재";
        const replacement = item.replacement ?? "미기재";
        return `| ${item.removalDate} | ${owner} | ${location(item, links)} | \`${item.declaration.replaceAll("|", "\\|").replaceAll("`", "'")}\` | ${replacement.replaceAll("|", "\\|")} |`;
    });
    return ["| 삭제 예정 | 담당자 | 위치 | 선언 | 대체 |", "|---|---|---|---|---|", ...rows].join("\n");
}

export function renderReport({ releaseDate, intervalDays, all, due, upcoming, missingOwner, unscheduled }, links = null) {
    const policyPath = "docs/guides/API_호환성_정책.md";
    const policy = links
        ? `[API 호환성 정책](https://github.com/${links.repository}/blob/${links.sha}/${encodeURI(policyPath)})`
        : `API 호환성 정책(\`${policyPath}\`)`;
    const sections = [
        REPORT_MARKER,
        `## 지원 중단 항목 점검 (${releaseDate} 릴리스)`,
        "",
        `${policy} 5절의 \`삭제 예정: YYYY-MM-DD 릴리스, 담당자: GitHub 아이디\` 표기를 기준으로 만든 목록입니다. 이 점검은 CI를 실패시키지 않습니다.`,
        "",
        `### 이번 릴리스에서 지워야 하는 항목 (${due.length})`,
        "",
        due.length
            ? `삭제 예정일이 이 릴리스(${releaseDate}) 이전인 항목입니다. 담당자는 호출이 없는지 확인하고 삭제하거나, 클라이언트 담당자와 새 삭제 예정일을 정해 주세요.\n\n${scheduledRows(due, links)}`
            : "없습니다.",
        "",
        `### 다음 릴리스에서 지울 예정인 항목 (${upcoming.length})`,
        "",
        upcoming.length
            ? `${addDays(releaseDate, intervalDays)}까지 삭제 예정인 항목입니다. 담당자는 영향받는 팀에 다시 알려 주세요.\n\n${scheduledRows(upcoming, links)}`
            : "없습니다.",
        "",
        `### 전체 지원 중단 목록 (${all.length})`,
        "",
        all.length
            ? `코드에 삭제 예정이 적힌 모든 항목입니다. 지워질 때까지 릴리스 PR마다 표시됩니다.\n\n${scheduledRows(all, links)}`
            : "없습니다.",
    ];
    if (missingOwner.length) {
        sections.push("", `### 담당자가 없는 항목 (${missingOwner.length})`, "", scheduledRows(missingOwner, links));
    }
    if (unscheduled.length) {
        const rows = unscheduled.map((item) => `- ${location(item, links)} \`${item.declaration.replaceAll("`", "'")}\``);
        sections.push(
            "",
            `### 삭제 예정이 적히지 않은 \`@Deprecated\` (${unscheduled.length})`,
            "",
            "<details><summary>목록 보기</summary>",
            "",
            ...rows,
            "",
            "</details>",
        );
    }
    return `${sections.join("\n")}\n`;
}

function parseArguments(argv) {
    const options = {};
    for (let index = 0; index < argv.length; index += 2) {
        const key = argv[index];
        const value = argv[index + 1];
        if (!key?.startsWith("--") || value === undefined) {
            throw new Error("usage: deprecation-report.mjs --root <dir> [--title <pr-title>] [--interval <days>] [--repository <owner/name> --sha <sha>]");
        }
        options[key.slice(2)] = value;
    }
    return options;
}

function main() {
    const options = parseArguments(process.argv.slice(2));
    const intervalDays = Number(options.interval ?? "14");
    if (!Number.isInteger(intervalDays) || intervalDays < 1) {
        throw new Error("Interval must be a positive integer");
    }
    const releaseDate = releaseDateFromTitle(options.title) ?? upcomingSunday();
    const { scheduled, unscheduled } = scanRepository(options.root ?? ".");
    const links = options.repository && options.sha ? { repository: options.repository, sha: options.sha } : null;
    process.stdout.write(renderReport({ releaseDate, intervalDays, unscheduled, ...classify(scheduled, releaseDate, intervalDays) }, links));
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    main();
}
