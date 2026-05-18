"""dev 브랜치 변경 내용으로 PR 본문을 생성한다.

흐름:
  1) git log/diff/stat을 수집해 변경 범위를 정리한다.
  2) Claude API에 diff/log 컨텍스트를 보내 요약과 추천 테스트 케이스를 JSON으로 받는다.
  3) 한국어 고정 양식(변경사항 / 체크할 사항 / 테스트 하는 법 / 추천 테스트 케이스)으로 조립해 stdout에 출력한다.

AI 호출이 실패하면 요약·테스트 케이스 섹션만 비워두고 나머지 본문은 그대로 생성한다.
"""

from __future__ import annotations

import json
import os
import subprocess
import sys
import urllib.error
import urllib.request

ANTHROPIC_URL = "https://api.anthropic.com/v1/messages"
ANTHROPIC_VERSION = "2023-06-01"
MODEL = "claude-haiku-4-5-20251001"
MAX_TOKENS = 1500
DIFF_CHAR_LIMIT = 180_000
TIMEOUT_SECONDS = 60


def run(cmd: list[str]) -> str:
    return subprocess.check_output(cmd, text=True, encoding="utf-8").strip()


def collect_git_context(base_branch: str) -> tuple[str, str, str]:
    run(["git", "fetch", "origin", base_branch])
    base = run(["git", "merge-base", f"origin/{base_branch}", "HEAD"])
    log = run(["git", "log", "--pretty=format:- %s", f"{base}..HEAD"])
    stat = run(["git", "diff", "--stat", f"{base}..HEAD"])
    diff = run(["git", "diff", f"{base}..HEAD"])
    if len(diff) > DIFF_CHAR_LIMIT:
        diff = diff[:DIFF_CHAR_LIMIT] + "\n\n... (이하 생략 — diff가 길어 잘라냄)"
    return log, stat, diff


def call_claude(log: str, stat: str, diff: str) -> tuple[str, list[str]]:
    api_key = os.environ.get("ANTHROPIC_API_KEY", "").strip()
    if not api_key:
        print("[generate_pr_body] ANTHROPIC_API_KEY 미설정 — AI 섹션 생략", file=sys.stderr)
        return "", []

    prompt = (
        "당신은 시니어 백엔드 개발자입니다. 아래 PR 변경 정보를 보고 두 가지를 한국어로 작성해주세요.\n\n"
        "1) summary: 이 PR이 무엇을 바꾸는지 2~4문장으로 요약. 불필요한 형용사·이모지 금지.\n"
        "2) test_cases: 이 변경 사항에 대해 나중에 작성하면 좋은 테스트 케이스 후보를 5~8개 추천. "
        "단위/슬라이스/통합 테스트를 적절히 섞고, '~를 검증한다' 형태로 끝맺어주세요. "
        "원자적 상태 전이·권한 검사·예외 경로 같은 경계 조건을 우선 포함하세요.\n\n"
        "반드시 다음 JSON 스키마로만 응답하세요. 다른 텍스트는 절대 출력하지 마세요.\n"
        '{"summary": "string", "test_cases": ["string", ...]}\n\n'
        "## 커밋 로그\n"
        f"{log or '(없음)'}\n\n"
        "## 변경 파일 통계\n"
        f"{stat or '(없음)'}\n\n"
        "## 통합 diff\n"
        f"{diff or '(없음)'}\n"
    )

    body = {
        "model": MODEL,
        "max_tokens": MAX_TOKENS,
        "messages": [{"role": "user", "content": prompt}],
    }
    req = urllib.request.Request(
        ANTHROPIC_URL,
        data=json.dumps(body).encode("utf-8"),
        headers={
            "x-api-key": api_key,
            "anthropic-version": ANTHROPIC_VERSION,
            "content-type": "application/json",
        },
    )

    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT_SECONDS) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, OSError) as e:
        print(f"[generate_pr_body] Claude API 호출 실패: {e}", file=sys.stderr)
        return "", []

    try:
        text = data["content"][0]["text"].strip()
        if text.startswith("```"):
            text = text.split("```", 2)[1]
            if text.startswith("json"):
                text = text[len("json"):]
            text = text.strip().rstrip("`").strip()
        parsed = json.loads(text)
        summary = str(parsed.get("summary", "")).strip()
        test_cases = [str(t).strip() for t in parsed.get("test_cases", []) if str(t).strip()]
        return summary, test_cases
    except (KeyError, IndexError, json.JSONDecodeError, TypeError) as e:
        print(f"[generate_pr_body] Claude 응답 파싱 실패: {e}", file=sys.stderr)
        return "", []


def assemble_body(log: str, stat: str, summary: str, test_cases: list[str]) -> str:
    summary_block = summary if summary else "<!-- AI 요약 생성 실패 — 직접 채워주세요 -->"
    log_block = log if log else "- (커밋 없음)"
    stat_block = stat if stat else "(변경 없음)"

    if test_cases:
        tests_block = "\n".join(f"- {c}" for c in test_cases)
    else:
        tests_block = "<!-- AI 추천 실패 — 다음 머지 전에 작성할 테스트를 직접 정리해주세요 -->"

    return f"""## 변경사항

{summary_block}

### 커밋 목록
{log_block}

### 주요 변경 파일
```
{stat_block}
```

## 체크할 사항
```
[ ] DB 스키마/마이그레이션 영향 확인
[ ] 인증/인가/CORS 등 보안 정책 영향 확인
[ ] 환경 변수·시크릿 추가/변경 여부
[ ] 프런트 API 계약 변경 여부
[ ] 신규 의존성 추가 여부
```

## 테스트 하는 법
- 코드 받기
```
git fetch origin dev
git checkout dev
```
- 백엔드 실행
```
./gradlew bootRun
```
- 프런트 실행
```
cd frontend
npm install
npm run dev
```
- 검증할 시나리오
  -
  -

## 추천 테스트 케이스
{tests_block}
"""


def main() -> int:
    base_branch = os.environ.get("BASE_BRANCH", "main")
    log, stat, diff = collect_git_context(base_branch)
    summary, test_cases = call_claude(log, stat, diff)
    body = assemble_body(log, stat, summary, test_cases)
    sys.stdout.write(body)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())