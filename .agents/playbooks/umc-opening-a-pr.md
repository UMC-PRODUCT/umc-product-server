---
extends: opening-a-pr, feature, bug-fix, refactoring
when: UMC 서버에서 커밋을 만들거나 PR을 열 때이며, Feature·Bug fix·Refactoring 끝의 Opening a PR 단계도 포함한다.
---

# UMC Opening a PR

pstack의 Opening a PR을 UMC 서버 규칙에 맞춘다. 커밋·PR 규칙의 원본은 루트 `AGENTS.md`의 `GIT-01`~`GIT-03`이고,
이 파일은 그 규칙을 pstack 단계에 어디에 끼우는지만 정한다. 규칙 문장은 이 파일에 옮겨 적지 않는다.

- **Replace** "Work from a git worktree off main." develop 기준 git worktree에서 작업한다. 이 레포의 기본 브랜치는 `main`이지만 개발 PR은 `develop`을 향한다(`GIT-02`). `.claude/settings.json`이 `worktree.baseRef`를 `head`로 두므로 worktree는 현재 HEAD에서 갈라진다. 시작 전에 `git fetch origin develop && git merge-base --is-ancestor origin/develop HEAD`로 HEAD가 최신 `develop`을 포함하는지 확인한다.
- **Replace** "Snarled worktree: reset from main, redo minimally." 꼬인 worktree는 `origin/develop`에서 다시 갈라 최소로 다시 한다.
- **After** "Commit bodies don't restate the subject." `GIT-03`을 따른다.
- **In** "Write every PR title, description, and commit body with `/technical-writing`" PR 제목, 본문, 커밋 메시지는 한국어로 쓴다. 식별자와 표준 기술 용어만 영어로 둔다.
- **Replace** "Conventional Commits `type(scope): subject`, no trailing period. Type such as `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, or `perf`. Scope is the area, such as `pstack` or `poteto-mode`. Subject is short, imperative, and names a real symbol when one carries the change, for example `fix(pstack): retarget opening-a-pr babysit trigger`." 커밋 메시지는 `GIT-01`, PR 제목은 `GIT-02`를 따른다. 제목은 태그 뒤에 무엇이 바뀌는지 한 줄로 쓴다.
- **Replace** "Use `##` headings, not bold lead-ins, in this order, and drop a section with nothing to say:" 바로 아래 섹션 목록(`## Why`부터 `## Verification`까지) 대신 UMC 형식을 쓴다. `.github/pull_request_template.md`의 두 섹션을 그대로 쓰고 템플릿의 HTML 주석은 지운다. 본문 전체는 약 40줄 이하로 쓴다. 넘치면 본문을 늘리기 전에 PR을 쪼갤 수 있는지 사용자에게 묻는다.
  - `## 🚀 작업 내용`
    1. 첫 문단에 왜 하는지를 한두 문장으로 쓴다. pstack이 `## Why`에 쓰라는 사용자 목표가 여기 들어간다.
    2. 가장 작은 그림 하나를 넣는다. 요청 흐름·트랜잭션·외부 호출 순서는 Mermaid `sequenceDiagram`, 상태 전이는 `stateDiagram-v2`, 호출 순서 변화는 앞뒤 맥락을 남긴 `diff` 스케치, 여러 파일·모듈에 걸친 변경은 경로마다 한 줄 설명을 붙인 짧은 파일 트리로 그린다. 이름은 diff에 있는 실제 클래스와 경로를 쓴다.
    3. 동작이 바뀌는 PR이면 `**Before:**`와 `**After:**`를 한 줄씩 쓴다. 실제로 실행해서 본 결과(테스트 이름, 응답 코드, 로그 한 줄)만 쓴다.
  - `## 💬 리뷰 중점사항`
    1. `| 영역 | 위험 | 봐줄 것 |` 표를 쓴다. 위험은 🔴 판단 필요, 🟡 확인, 🟢 훑기 중 하나이고 🔴 행부터 적는다. 운영 동작, API 계약, DB 마이그레이션, 권한 변경은 🔴이다.
    2. `**되돌리기:**` 뒤에 쉬움 또는 어려움과 이유를 한 문장으로 쓴다. 마이그레이션, 데이터 변경, 외부 발송·공개는 어려움이다.
    3. `**검증:**` 뒤에 실제로 돌린 명령과 결과를 한 줄로 쓴다. 돌리지 않은 것은 쓰지 않는다.
  - 문장은 짧게 쓰고, 인라인 코드는 리뷰어가 찾아야 하는 이름에만 쓴다. 전체 로그나 대조 결과 표 같은 자세한 근거는 PR 코멘트로 남기고 본문에는 결론만 쓴다.
- **In** "The root PR targets trunk." trunk는 `develop`이다(`GIT-02`). 스택이 아닌 단일 개발 PR도 base를 `develop`으로 연다(`gh pr create --base develop`).
- **After** "Open every PR ready, never as a draft." PR을 연 뒤 `gh pr view`로 base가 `develop`인지 확인한다. 그다음 `/pstack:thermos`로 PR head를 리뷰하고, 결과는 `docs/guides/코드_리뷰_기준.md`의 "AI 리뷰를 돌리고 PR에 남기는 방법"대로 GitHub PR에 리뷰로 남긴다. 지적 처리도 같은 절차를 따른다.
