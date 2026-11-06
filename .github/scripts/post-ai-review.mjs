import { readFileSync } from "node:fs";
import { pathToFileURL } from "node:url";

const REVIEWERS = {
    bug: "🐛 버그·보안 리뷰어",
    quality: "🧹 코드 품질 리뷰어",
};

const PRIORITIES = ["P1", "P2", "P3", "P4", "P5"];
const REVIEW_AUTHOR = "github-actions[bot]";
const ZERO_WIDTH_SPACE = String.fromCodePoint(0x200b);
const SECRET_PATTERN = /(ghp_|ghs_|gho_|github_pat_|sk-ant-)[A-Za-z0-9_-]{10,}/;

export function parseDiffLines(patch) {
    const linesByPath = new Map();
    let lines = null;
    let next = 0;
    let inHunk = false;

    for (const text of patch.split("\n")) {
        if (text.startsWith("diff --git ")) {
            lines = null;
            inHunk = false;
        } else if (text.startsWith("+++ ") && !inHunk) {
            const path = text.slice(4).trim();
            lines = path === "/dev/null" ? null : new Set();
            if (lines) {
                linesByPath.set(path.replace(/^b\//, ""), lines);
            }
        } else if (text.startsWith("@@ ")) {
            next = Number(/\+(\d+)/.exec(text)[1]);
            inHunk = true;
        } else if (inHunk && lines && (text.startsWith("+") || text.startsWith(" "))) {
            lines.add(next);
            next += 1;
        }
    }
    return linesByPath;
}

// 코드 밖의 `@Transactional` 같은 글자가 같은 이름의 GitHub 사용자에게 알림을 보내지 않게 한다.
export function neutralizeMentions(text) {
    return text
        .split(/(```[\s\S]*?```|`[^`\n]*`)/)
        .map((part, index) => (index % 2 === 1 ? part : part.replace(/@(?=[A-Za-z0-9-])/g, `@${ZERO_WIDTH_SPACE}`)))
        .join("");
}

function fenced(info, content) {
    const longest = Math.max(0, ...(content.match(/`+/g) ?? []).map((run) => run.length));
    const fence = "`".repeat(Math.max(3, longest + 1));
    return `${fence}${info}\n${content}\n${fence}`;
}

function findingTitle(finding) {
    const tags = finding.rule ? `[${finding.priority}][${neutralizeMentions(finding.rule)}]` : `[${finding.priority}]`;
    const overlap = finding.also_by_other_reviewer ? " (다른 리뷰어와 겹침)" : "";
    return `**${tags} ${neutralizeMentions(finding.title)}**${overlap}`;
}

function findingExplanation(finding) {
    return [
        neutralizeMentions(finding.problem),
        ...(finding.when ? [`**언제 터지나요:** ${neutralizeMentions(finding.when)}`] : []),
        `**고치는 방법:** ${neutralizeMentions(finding.fix)}`,
    ].join("\n\n");
}

function isRange(finding) {
    return Boolean(finding.start_line) && finding.start_line < finding.line;
}

function isInline(finding, diffLines) {
    const lines = diffLines.get(finding.path);
    const start = finding.start_line ?? finding.line;
    if (!lines || start > finding.line) {
        return false;
    }
    for (let line = start; line <= finding.line; line += 1) {
        if (!lines.has(line)) {
            return false;
        }
    }
    return true;
}

function toComment(finding) {
    const suggestion = finding.suggestion ? `\n\n${fenced("suggestion", finding.suggestion)}` : "";
    return {
        path: finding.path,
        line: finding.line,
        side: "RIGHT",
        ...(isRange(finding) ? { start_line: finding.start_line, start_side: "RIGHT" } : {}),
        body: `${findingTitle(finding)}\n\n${findingExplanation(finding)}${suggestion}`,
    };
}

function permalink(finding, { repository, head }) {
    const path = finding.path.split("/").map(encodeURIComponent).join("/");
    const range = isRange(finding) ? `#L${finding.start_line}-L${finding.line}` : `#L${finding.line}`;
    return `https://github.com/${repository}/blob/${head}/${path}${range}`;
}

function toBodySection(finding, context) {
    const suggestion = finding.suggestion ? `\n\n${fenced("", finding.suggestion)}` : "";
    return `${findingTitle(finding)}\n\n${permalink(finding, context)}\n\n${findingExplanation(finding)}${suggestion}`;
}

function details(summary, items) {
    if (items.length === 0) {
        return [];
    }
    const list = items.map((item) => `- ${neutralizeMentions(item)}`).join("\n");
    return [`<details>\n<summary>${summary} ${items.length}건</summary>\n\n${list}\n\n</details>`];
}

export function buildReview(reviewer, context) {
    const inline = reviewer.findings.filter((finding) => isInline(finding, context.diffLines));
    const outside = reviewer.findings.filter((finding) => !isInline(finding, context.diffLines));
    const counts = PRIORITIES.map((priority) => reviewer.findings.filter((f) => f.priority === priority).length);
    const body = [
        REVIEWERS[reviewer.reviewer],
        `| ${PRIORITIES.join(" | ")} |\n|${"---|".repeat(PRIORITIES.length)}\n| ${counts.join(" | ")} |`,
        ...outside.map((finding) => toBodySection(finding, context)),
        ...details("문제없다고 확인한 것", reviewer.checked_ok),
        ...details("확인해 보니 문제가 아니어서 뺀 지적", reviewer.excluded.map((e) => `${e.title}: ${e.reason}`)),
    ].join("\n\n");

    return { commit_id: context.head, event: "COMMENT", body, comments: inline.map(toComment) };
}

export function extractResult(raw) {
    const result = JSON.parse(raw).findLast((entry) => entry.type === "result")?.structured_output;
    const names = (result?.reviewers ?? []).map((reviewer) => reviewer.reviewer).sort();
    if (names.join() !== "bug,quality") {
        throw new Error(`리뷰 결과의 리뷰어가 bug와 quality 하나씩이 아닙니다: ${names.join()}`);
    }
    if (SECRET_PATTERN.test(JSON.stringify(result))) {
        throw new Error("리뷰 결과에 토큰처럼 보이는 값이 있어 게시하지 않습니다.");
    }
    return result;
}

export function firstLinesAt(reviews, head) {
    return reviews
        .filter((review) => review.commit_id === head && review.user?.login === REVIEW_AUTHOR)
        .map((review) => (review.body ?? "").split("\n")[0]);
}

async function github(method, url, token, body) {
    const response = await fetch(`https://api.github.com/${url}`, {
        method,
        headers: {
            Accept: "application/vnd.github+json",
            Authorization: `Bearer ${token}`,
            "X-GitHub-Api-Version": "2022-11-28",
        },
        body: body ? JSON.stringify(body) : undefined,
    });
    return { status: response.status, ok: response.ok, data: await response.json() };
}

async function listReviews(repository, number, token) {
    const reviews = [];
    for (let page = 1; ; page += 1) {
        const { ok, status, data } = await github("GET", `repos/${repository}/pulls/${number}/reviews?per_page=100&page=${page}`, token);
        if (!ok) {
            throw new Error(`리뷰 목록을 읽지 못했습니다: ${status}`);
        }
        reviews.push(...data);
        if (data.length < 100) {
            return reviews;
        }
    }
}

async function postReview(reviewer, context, number, token) {
    const url = `repos/${context.repository}/pulls/${number}/reviews`;
    let response = await github("POST", url, token, buildReview(reviewer, context));
    if (response.status === 422) {
        // 줄 판정이 GitHub와 어긋나도 지적을 잃지 않도록 전부 본문으로 옮겨 다시 보낸다.
        response = await github("POST", url, token, buildReview(reviewer, { ...context, diffLines: new Map() }));
    }
    if (!response.ok) {
        throw new Error(`${reviewer.reviewer} 리뷰를 남기지 못했습니다: ${response.status} ${JSON.stringify(response.data)}`);
    }
    return response.data.html_url;
}

export async function run({ RESULT_FILE, DIFF_FILE, HEAD_SHA, PR_NUMBER, GITHUB_REPOSITORY, GITHUB_TOKEN }) {
    const result = extractResult(readFileSync(RESULT_FILE, "utf8"));
    const context = {
        repository: GITHUB_REPOSITORY,
        head: HEAD_SHA,
        diffLines: parseDiffLines(readFileSync(DIFF_FILE, "utf8")),
    };
    const posted = firstLinesAt(await listReviews(GITHUB_REPOSITORY, PR_NUMBER, GITHUB_TOKEN), HEAD_SHA);

    for (const reviewer of result.reviewers) {
        if (posted.includes(REVIEWERS[reviewer.reviewer])) {
            console.log(`이미 남긴 리뷰라 건너뜁니다: ${reviewer.reviewer}`);
        } else {
            console.log(await postReview(reviewer, context, PR_NUMBER, GITHUB_TOKEN));
        }
    }
}

if (import.meta.url === pathToFileURL(process.argv[1]).href) {
    run(process.env).catch((error) => {
        console.error(`::error::${error.message}`);
        process.exit(1);
    });
}
