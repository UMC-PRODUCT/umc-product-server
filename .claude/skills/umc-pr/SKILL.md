---
name: umc-pr
description: Use when writing a PR body or running `gh pr create` in the UMC server repo. PR 글쓰기 기준대로 본문을 쓰고 올리기 전에 점검한다.
---

# UMC PR 본문

먼저 읽는다: `docs/guides/PR_글쓰기_기준.md`, `.github/pull_request_template.md`, 루트 `AGENTS.md`의 `GIT-01`~`GIT-03`. 본문 뼈대와 글쓰기 규칙은 기준 문서에 있다.

1. 기준 문서의 "PR 본문 모양"대로 쓰고 템플릿의 HTML 주석은 지운다.
2. 제목과 본문을 기준 문서의 규칙과 하나씩 대조해 어긋난 곳을 고친다. 리뷰 지적 규칙은 대조하지 않는다.
3. `gh pr create --base develop`로 열고 본문은 `--body-file`로 넘긴다. `gh pr view`로 base를 확인한다.
