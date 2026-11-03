// `preview` 라벨이 붙은 순간에만 자리와 develop 마이그레이션 반영 여부를 판정한다.
// 새 dev 복사는 이때만 일어나므로, 이후 새 커밋 배포에서는 다시 판정하지 않는다.

export const PREVIEW_SLOT_LIMIT = 2;
export const MIGRATION_DIRECTORY = "monolith/src/main/resources/db/migration";
export const ADMISSION_COMMENT_MARKER = "<!-- umc-preview-admission -->";

const MIGRATION_FILE = /^V([0-9]+(?:\.[0-9]+)*)__[A-Za-z0-9_]+\.sql$/;
const LISTED_LIMIT = 10;

export function parseMigrationVersion(fileName) {
    const match = MIGRATION_FILE.exec(fileName);
    return match ? match[1].split(".").map(Number) : null;
}

export function compareMigrationVersions(left, right) {
    for (let index = 0; index < Math.max(left.length, right.length); index += 1) {
        const difference = (left[index] ?? 0) - (right[index] ?? 0);
        if (difference !== 0) {
            return Math.sign(difference);
        }
    }
    return 0;
}

export function checkMigrationLag(baseFiles, headFiles) {
    const base = baseFiles.filter((name) => parseMigrationVersion(name));
    const head = new Set(headFiles.filter((name) => parseMigrationVersion(name)));
    const missing = base.filter((name) => !head.has(name)).sort();
    const latestBase = base
        .map(parseMigrationVersion)
        .reduce((latest, version) => (!latest || compareMigrationVersions(version, latest) > 0 ? version : latest), null);
    // out-of-order가 꺼져 있어 develop 최신보다 낮은 새 버전은 dev DB에 적용되지 않고 기동을 막는다.
    const baseNames = new Set(base);
    const outdated = [...head]
        .filter((name) => !baseNames.has(name))
        .filter((name) => latestBase && compareMigrationVersions(parseMigrationVersion(name), latestBase) < 0)
        .sort();
    return { missing, outdated };
}

function listed(names) {
    const lines = names.slice(0, LISTED_LIMIT).map((name) => `- \`${name}\``);
    if (names.length > LISTED_LIMIT) {
        lines.push(`- 외 ${names.length - LISTED_LIMIT}개`);
    }
    return lines.join("\n");
}

function admittedComment(prNumber) {
    return [
        ADMISSION_COMMENT_MARKER,
        "### Preview 자리 배정",
        "",
        `이 PR이 preview 자리(동시 최대 ${PREVIEW_SLOT_LIMIT}개)를 받았습니다. 이미지 발행이 끝나면 아래 주소가 열립니다.`,
        "",
        `- API 문서: https://api-pr-${prNumber}.university.neordinary.com/docs/scalar.html (Preview 공용 Basic Auth)`,
        "- `preview` 라벨을 떼거나 PR을 닫으면 이 PR의 preview DB가 삭제됩니다. 필요한 테스트 결과는 미리 저장하세요.",
        "- 라벨을 다시 붙이면 develop 마이그레이션 검사와 이미지 발행을 다시 거쳐 새로 만듭니다.",
    ].join("\n");
}

function slotsFullComment(otherPreviewPrNumbers) {
    return [
        ADMISSION_COMMENT_MARKER,
        "### Preview 자리 없음",
        "",
        `preview는 동시에 ${PREVIEW_SLOT_LIMIT}개까지만 띄울 수 있어 \`preview\` 라벨을 뗐습니다.`,
        `지금 사용 중: ${otherPreviewPrNumbers.map((number) => `#${number}`).join(", ")}`,
        "",
        "자리가 나면 라벨을 다시 붙여 주세요.",
    ].join("\n");
}

function migrationLagComment({ missing, outdated }) {
    const sections = [
        ADMISSION_COMMENT_MARKER,
        "### develop 마이그레이션 반영 필요",
        "",
        "preview DB는 dev DB를 복사해 만들기 때문에, 이 상태로는 앱이 뜨지 않아 `preview` 라벨을 뗐습니다.",
    ];
    if (missing.length) {
        sections.push("", "develop에는 있는데 이 PR에는 없는 마이그레이션입니다. develop을 merge해 주세요.", "", listed(missing));
    }
    if (outdated.length) {
        sections.push(
            "",
            "이 PR의 새 마이그레이션 버전이 develop 최신보다 낮습니다. dev 배포에서도 같은 이유로 실패하므로 버전을 develop 최신 뒤로 바꿔 주세요.",
            "",
            listed(outdated),
        );
    }
    sections.push("", "고친 뒤 라벨을 다시 붙여 주세요.");
    return sections.join("\n");
}

export function decideAdmission({ prNumber, otherPreviewPrNumbers, baseFiles, headFiles }) {
    if (otherPreviewPrNumbers.length >= PREVIEW_SLOT_LIMIT) {
        return { admitted: false, comment: slotsFullComment([...otherPreviewPrNumbers].sort((a, b) => a - b)) };
    }
    const lag = checkMigrationLag(baseFiles, headFiles);
    if (lag.missing.length || lag.outdated.length) {
        return { admitted: false, comment: migrationLagComment(lag) };
    }
    return { admitted: true, comment: admittedComment(prNumber) };
}
