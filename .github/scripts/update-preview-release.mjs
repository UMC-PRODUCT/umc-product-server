import { execFileSync } from "node:child_process";
import { lstatSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

function hasExactKeys(value, keys) {
    return value !== null && typeof value === "object" && !Array.isArray(value)
        && Object.keys(value).sort().join(",") === [...keys].sort().join(",");
}

function previewReleasePath(prNumber) {
    if (typeof prNumber !== "string" || !/^[1-9][0-9]*$/.test(prNumber)
        || !Number.isSafeInteger(Number(prNumber)) || prNumber !== String(Number(prNumber))) {
        throw new Error("PR number must be a positive canonical integer string");
    }
    return `argocd/preview-releases/pr-${prNumber}.json`;
}

export function validatePreviewRelease(document) {
    if (!hasExactKeys(document, ["release"])
        || !hasExactKeys(document.release, ["prNumber", "headSha", "imageTag", "imageDigest"])) {
        throw new Error("Preview release schema has missing or unexpected fields");
    }
    const { prNumber, headSha, imageTag, imageDigest } = document.release;
    const markerPath = previewReleasePath(prNumber);
    if (typeof headSha !== "string" || headSha.length !== 40 || !/^[a-f0-9]{40}$/.test(headSha)) {
        throw new Error("PR head must be a full lowercase commit SHA");
    }
    if (typeof imageTag !== "string" || imageTag !== headSha.slice(0, 12)) {
        throw new Error("Image tag must match the first 12 characters of the PR head");
    }
    if (typeof imageDigest !== "string" || imageDigest.length !== 71 || !/^sha256:[a-f0-9]{64}$/.test(imageDigest)) {
        throw new Error("Image digest must be a lowercase sha256 digest");
    }
    return markerPath;
}

function inspectPath(path, directory) {
    const stat = lstatSync(path, { throwIfNoEntry: false });
    if (stat && (stat.isSymbolicLink() || !(directory ? stat.isDirectory() : stat.isFile()))) {
        throw new Error("Preview release paths must be regular directories and files, not symlinks");
    }
    return stat;
}

export function assertOnlyPreviewReleaseChanged(repository, markerPath) {
    // untracked 파일도 포함한다. rename/copy/delete 및 대상 외 변경은 commit 전에 거부한다.
    const status = execFileSync("git", ["status", "--porcelain=v1", "-z", "--untracked-files=all"], {
        cwd: repository,
        encoding: "utf8",
    });
    for (const entry of status.split("\0").filter(Boolean)) {
        if (!["??", " M", "M ", "A ", " D", "D "].includes(entry.slice(0, 2)) || entry.slice(3) !== markerPath) {
            throw new Error("Infrastructure has changes outside the selected Preview release");
        }
    }
}

function resolveMarker(repository, markerPath) {
    const root = resolve(repository);
    if (!inspectPath(root, true) || !inspectPath(resolve(root, "argocd"), true)) {
        throw new Error("Expected an infrastructure checkout containing argocd");
    }
    const directory = resolve(root, "argocd/preview-releases");
    inspectPath(directory, true);
    const target = resolve(root, markerPath);
    const exists = inspectPath(target, false);
    assertOnlyPreviewReleaseChanged(root, markerPath);
    return { root, directory, target, exists };
}

function readExistingRelease(target, markerPath) {
    const previous = readFileSync(target, "utf8");
    if (validatePreviewRelease(JSON.parse(previous)) !== markerPath) {
        throw new Error("Existing release PR number does not match its filename");
    }
    return previous;
}

export function updatePreviewRelease(repository, document) {
    const markerPath = validatePreviewRelease(document);
    const { root, directory, target, exists } = resolveMarker(repository, markerPath);
    const previous = exists ? readExistingRelease(target, markerPath) : null;
    const updated = `${JSON.stringify(document, null, 2)}\n`;
    if (previous === updated) {
        return { markerPath, changed: false };
    }
    mkdirSync(directory, { recursive: true });
    writeFileSync(target, updated, "utf8");
    assertOnlyPreviewReleaseChanged(root, markerPath);
    return { markerPath, changed: true };
}

// 라벨 제거·PR 종료 때 기록을 지워, 다시 라벨을 붙이면 검사와 발행을 거친 뒤에만 preview가 생기게 한다.
export function removePreviewRelease(repository, prNumber) {
    const markerPath = previewReleasePath(prNumber);
    const { root, target, exists } = resolveMarker(repository, markerPath);
    if (!exists) {
        return { markerPath, changed: false };
    }
    readExistingRelease(target, markerPath);
    rmSync(target);
    assertOnlyPreviewReleaseChanged(root, markerPath);
    return { markerPath, changed: true };
}

function main() {
    if (process.argv[2] === "remove") {
        const [repository, prNumber, ...extra] = process.argv.slice(3);
        if (!repository || extra.length) {
            throw new Error("usage: update-preview-release.mjs remove <infra-checkout> <pr-number>");
        }
        const result = removePreviewRelease(repository, prNumber);
        console.log(`${result.markerPath}: ${result.changed ? "removed" : "absent"}`);
        return;
    }
    const [repository, prNumber, headSha, imageTag, imageDigest, ...extra] = process.argv.slice(2);
    if (!repository || extra.length) {
        throw new Error("usage: update-preview-release.mjs <infra-checkout> <pr-number> <head-sha> <tag> <digest>");
    }
    const result = updatePreviewRelease(repository, {
        release: { prNumber, headSha, imageTag, imageDigest },
    });
    console.log(`${result.markerPath}: ${result.changed ? "updated" : "unchanged"}`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    main();
}
