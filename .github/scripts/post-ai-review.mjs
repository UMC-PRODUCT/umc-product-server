import { readFileSync } from "node:fs";
import { pathToFileURL } from "node:url";

export const REVIEWERS = {
    bug: "🐛 버그·보안 리뷰어",
    quality: "🧹 코드 품질 리뷰어",
};

const PRIORITIES = ["P1", "P2", "P3", "P4", "P5"];
const REVIEW_AUTHOR = "github-actions[bot]";

// git은 한글 경로를 "b/docs/PR_\352\270\200.md"처럼 따옴표와 8진수로 적는다.
function decodeDiffPath(path) {
    if (!path.startsWith('"')) {
        return path;
    }
    const bytes = [];
    const inner = path.slice(1, -1);
    for (let index = 0; index < inner.length; index += 1) {
        if (inner[index] !== "\\") {
            bytes.push(...Buffer.from(inner[index]));
        } else if (/[0-7]/.test(inner[index + 1])) {
            bytes.push(Number.parseInt(inner.slice(index + 1, index + 4), 8));
            index += 3;
        } else {
            bytes.push(...Buffer.from(inner[index + 1]));
            index += 1;
        }
    }
    return Buffer.from(bytes).toString("utf8");
}

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
            const path = decodeDiffPath(text.slice(4).trim());
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
        .map((part, index) => (index % 2 === 1 ? part : part.replace(/@(?=[A-Za-z0-9-])/g, "@​")))
        .join("");
}

function findingTitle(finding) {
    const tags = finding.rule ? `[${finding.priority}][${finding.rule}]` : `[${finding.priority}]`;
    const overlap = finding.also_by_other_reviewer ? " (다른 리뷰어와 겹침)" : "";
    return `**${tags} ${neutralizeMentions(finding.title)}**${overlap}`;
}

function findingExplanation(finding) {
    return [
        neutralizeMentions(finding.problem),
        `**언제 터지나요:** ${neutralizeMentions(finding.when)}`,
        `**고치는 방법:** ${neutralizeMentions(finding.fix)}`,
    ].join("\n\n");
}

function isInline(finding, diffLines) {
    const lines = diffLines.get(finding.path);
    if (!lines) {
        return false;
    }
    for (let line = finding.start_line ?? finding.line; line <= finding.line; line += 1) {
        if (!lines.has(line)) {
            return false;
        }
    }
    return true;
}

function toComment(finding) {
    const suggestion = finding.suggestion ? `\n\n\`\`\`suggestion\n${finding.suggestion}\n\`\`\`` : "";
    const range = finding.start_line && finding.start_line < finding.line
        ? { start_line: finding.start_line, start_side: "RIGHT" }
        : {};
    return {
        path: finding.path,
        line: finding.line,
        side: "RIGHT",
        ...range,
        body: `${findingTitle(finding)}\n\n${findingExplanation(finding)}${suggestion}`,
    };
}

function permalink(finding, { repository, head }) {
    const path = finding.path.split("/").map(encodeURIComponent).join("/");
    const range = finding.start_line && finding.start_line < finding.line
        ? `#L${finding.start_line}-L${finding.line}`
        : `#L${finding.line}`;
    return `https://github.com/${repository}/blob/${head}/${path}${range}`;
}

function toBodySection(finding, context) {
    return `${findingTitle(finding)}\n\n${permalink(finding, context)}\n\n${findingExplanation(finding)}`;
}

function details(summary, items) {
    if (items.length === 0) {
        return [];
    }
    const list = items.map((item) => `- ${neutralizeMentions(item)}`).join("\n");
    return [`<details>\n<summary>${summary} ${items.length}건</summary>\n\n${list}\n\n</details>`];
}

function reviewBody(reviewer, outside, context) {
    const counts = PRIORITIES.map((priority) => reviewer.findings.filter((f) => f.priority === priority).length);
    return [
        REVIEWERS[reviewer.reviewer],
        `| ${PRIORITIES.join(" | ")} |\n|${"---|".repeat(PRIORITIES.length)}\n| ${counts.join(" | ")} |`,
        ...outside.map((finding) => toBodySection(finding, context)),
        ...details("문제없다고 확인한 것", reviewer.checked_ok),
        ...details("확인해 보니 문제가 아니어서 뺀 지적", reviewer.excluded.map((e) => `${e.title}: ${e.reason}`)),
    ].join("\n\n");
}

export function buildReview(reviewer, context) {
    const inline = reviewer.findings.filter((finding) => isInline(finding, context.diffLines));
    const outside = reviewer.findings.filter((finding) => !isInline(finding, context.diffLines));
    return {
        commit_id: context.head,
        event: "COMMENT",
        body: reviewBody(reviewer, outside, context),
        comments: inline.map(toComment),
    };
}

// 줄 판정이 GitHub와 어긋나 422가 나도 지적을 잃지 않도록 전부 본문으로 옮긴 판을 만든다.
export function buildBodyOnlyReview(reviewer, context) {
    return {
        commit_id: context.head,
        event: "COMMENT",
        body: reviewBody(reviewer, reviewer.findings, context),
        comments: [],
    };
}

export function extractResult(raw) {
    const parsed = JSON.parse(raw);
    const entry = Array.isArray(parsed) ? parsed.findLast((item) => item.type === "result") : parsed;
    const result = entry?.structured_output ?? entry;
    if (!Array.isArray(result?.reviewers) || result.reviewers.length === 0) {
        throw new Error("리뷰 결과에 reviewers가 없습니다.");
    }
    for (const reviewer of result.reviewers) {
        if (!REVIEWERS[reviewer.reviewer]) {
            throw new Error(`알 수 없는 리뷰어입니다: ${reviewer.reviewer}`);
        }
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
    const first = await github("POST", url, token, buildReview(reviewer, context));
    if (first.ok) {
        return first.data.html_url;
    }
    if (first.status !== 422) {
        throw new Error(`${reviewer.reviewer} 리뷰를 남기지 못했습니다: ${first.status} ${JSON.stringify(first.data)}`);
    }
    const second = await github("POST", url, token, buildBodyOnlyReview(reviewer, context));
    if (!second.ok) {
        throw new Error(`${reviewer.reviewer} 리뷰를 남기지 못했습니다: ${second.status} ${JSON.stringify(second.data)}`);
    }
    return second.data.html_url;
}

async function main() {
    const { RESULT_FILE, DIFF_FILE, HEAD_SHA, PR_NUMBER, GITHUB_REPOSITORY, GITHUB_TOKEN } = process.env;
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
    main().catch((error) => {
        console.error(`::error::${error.message}`);
        process.exit(1);
    });
}
