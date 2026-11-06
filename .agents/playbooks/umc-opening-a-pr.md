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
- **Replace** "Use `##` headings, not bold lead-ins, in this order, and drop a section with nothing to say:" 바로 아래 섹션 목록(`## Why`부터 `## Verification`까지) 대신 `/umc-pr` 스킬로 본문을 쓴다. 뼈대는 그 스킬에, 글쓰기 규칙은 `docs/guides/PR_글쓰기_기준.md`에 있다. pstack이 `## Why`에 쓰라는 사용자 목표는 `## 🚀 작업 내용`의 첫 문단에 들어간다.
- **In** "The root PR targets trunk." trunk는 `develop`이다(`GIT-02`). 스택이 아닌 단일 개발 PR도 base를 `develop`으로 연다(`gh pr create --base develop`).
- **After** "Open every PR ready, never as a draft." PR을 연 뒤 `gh pr view`로 base가 `develop`인지 확인한다. 리뷰는 CI가 자동으로 달며, 다시 받으려면 `ai-review` 라벨을 붙인다. 지적 처리는 `docs/guides/코드_리뷰_기준.md`의 "AI 리뷰" 절차를 따른다.
