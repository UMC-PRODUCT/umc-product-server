---
extends: opening-a-pr, feature, bug-fix, refactoring
when: UMC 서버에서 커밋을 만들거나 PR을 열 때이며, Feature·Bug fix·Refactoring 끝의 Opening a PR 단계도 포함한다.
---

# UMC Opening a PR

pstack의 Opening a PR을 UMC 서버 규칙에 맞춘다. 규칙의 원본은 루트 `AGENTS.md`의 REVIEW AND GIT 절이다.
이 파일과 `AGENTS.md`가 다르면 `AGENTS.md`를 따른다.

- **Replace** "Work from a git worktree off main." develop 기준 git worktree에서 작업한다. 이 레포의 기본 브랜치는 `main`이지만 개발 PR은 `develop`을 향한다. `.claude/settings.json`이 `worktree.baseRef`를 `head`로 두므로 worktree는 현재 HEAD에서 갈라진다. 시작 전에 HEAD가 최신 `origin/develop`을 포함하는지 확인한다.
- **After** "Commit bodies don't restate the subject." 커밋과 PR에 AI 작성자, AI 커미터 메타데이터, `Co-authored-by` 트레일러를 넣지 않는다.
- **In** "Write every PR title, description, and commit body with `/technical-writing`" PR 제목, 본문, 커밋 메시지는 한국어로 쓴다. 식별자와 표준 기술 용어만 영어로 둔다.
- **Replace** "Conventional Commits `type(scope): subject`, no trailing period. Type such as `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, or `perf`. Scope is the area, such as `pstack` or `poteto-mode`. Subject is short, imperative, and names a real symbol when one carries the change, for example `fix(pstack): retarget opening-a-pr babysit trigger`." 커밋 메시지는 `<type>: <subject>` 형식이고 type은 `feat`, `fix`, `refactor`, `docs`, `test`, `chore` 중 하나다. PR 제목은 `[Feat]`, `[Fix]`, `[HotFix]`, `[Refactor]`, `[Chore]`, `[Docs]` 태그 뒤에 무엇이 바뀌는지 한 줄로 쓴다. `[Release]`는 릴리스 PR에만 쓴다.
- **Replace** "Use `##` headings, not bold lead-ins, in this order, and drop a section with nothing to say:" `.github/pull_request_template.md`의 두 섹션 `## 🚀 작업 내용`과 `## 💬 리뷰 중점사항`을 그대로 쓰고 템플릿의 HTML 주석은 지운다. 아래 항목의 내용은 그 두 섹션 안에 짧은 소제목이나 글머리표로 넣는다. Why, What changed, Scope는 작업 내용에, Tradeoffs, Blast Radius, Verification은 리뷰 중점사항에 둔다. 할 말이 없는 항목은 뺀다.
- **In** "The root PR targets trunk." trunk는 `develop`이다. 스택이 아닌 단일 개발 PR도 base를 `develop`으로 연다(`gh pr create --base develop`). `main`을 향하는 PR은 릴리스 PR뿐이다.
- **After** "Open every PR ready, never as a draft." PR을 연 뒤 `gh pr view`로 base가 `develop`인지 확인한다.
