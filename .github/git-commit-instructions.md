# Git Commit Convention

## Required: Use Unicode Gitmoji

**IMPORTANT**: Always use Unicode emoji characters (✨, ♻️, 🐛, etc.), NOT text codes (`:sparkles:`, `:recycle:`, `:bug:`).

### Gitmoji Reference Table

| Emoji | Type | When to Use |
| --- | --- | --- |
| 🎉 | init | Begin a project (initial commit) |
| ✨ | feat | Introduce new features or feature-facing code flows |
| ♻️ | refactor | Refactor code without changing behavior |
| 🔥 | remove | Remove code or files |
| 🐛 | fix | Fix a bug |
| ✅ | test | Add or update tests |
| 📝 | docs | Add or update documentation |
| ➕ | deps | Add a dependency |
| ➖ | deps | Remove a dependency |
| 🔧 | config | Add or update configuration files |
| ⏪ | revert | Revert changes |
| 🚚 | rename | Move or rename resources, files, paths, or routes |
| 💡 | comment | Add or update comments in source code |

## Commit Message Format

```text
<gitmoji> <type>: <subject>

<body>

Ref: #<jira-ticket>
```

### Structure Requirements

- **Line 1**: `<gitmoji> <type>: <subject>` (max 50 characters)
- **Line 2**: Empty line
- **Line 3+**: Body (bullet points with `-`)
- **Last section**: `Ref: #<jira-ticket>` (separated by empty line)

## Examples

### Initial Commit Example

```text
🎉 init: Spring Boot 애플리케이션 초기 설정

- Spring Boot 프로젝트 기본 구조 추가
- Gradle Wrapper와 애플리케이션 진입점 구성
- 기본 application.yaml 설정 추가

Ref: #S14P31A209-1
```

### Backend Config Example

```text
🔧 config: 로컬 데이터 인프라 구성

- PostgreSQL, Redis Docker Compose 추가
- Redis 영속화 및 메모리 정책 설정
- 로컬 실행용 환경 변수 예시 추가

Ref: #S14P31A209-12
```

### Feature Example

```text
✨ feat: 도메인 계층 작성 예제 구성

- controller, facade, command, query 계층 예시 추가
- 요청/응답 DTO와 converter 작성 방식 제시
- 도메인 예외와 에러 코드 연결

Ref: #S14P31A209-24
```

## Subject Line Rules

### Correct Examples

```text
🎉 init: Spring Boot 애플리케이션 초기 설정
✨ feat: 도메인 계층 작성 예제 구성
♻️ refactor: 공통 응답 생성 흐름 정리
🐛 fix: 환경 변수 바인딩 오류 수정
📝 docs: 프로젝트 작성 가이드 정리
```

### Incorrect Examples

```text
✨ feat: 도메인 계층 작성 예제 구성.       // No period at end
✨ Feat: 도메인 계층 작성 예제 구성        // Type must be lowercase
✨ feat: 도메인 계층을 작성한다            // Use imperative/noun style, not verb endings
✨ feat: 도메인 계층을 작성했습니다         // Do not use past tense
✨ feat(#12): 도메인 계층 작성 예제 구성    // Do not put ticket in subject
```

### Subject Line Requirements

1. **Max 50 characters** (한글 기준 약 25자 이내)
2. **Start with gitmoji** (Unicode character, not text)
3. **Type in lowercase** (feat, fix, refactor, etc.)
4. **Do not include ticket number in subject**
5. **Use imperative or noun style**: "추가", "구성", "정리", "수정"
6. **No period** at the end
7. **Write in Korean** (subject and body)

## Body Rules

### Format

- Use `-` (dash) to list changes
- Each bullet point on a new line
- Focus on **why this logical unit exists** and **what changed**
- Be specific and concise

### Correct Example

```text
- Checkstyle 플러그인과 envio_auth 규칙 파일 연결
- Naver Java Convention 기반 검사 규칙 추가
- main 소스 품질 검사 실패 시 빌드 실패 처리
```

### Incorrect Example

```text
여러 파일 수정                         // Too vague
- build.gradle 수정                    // File-oriented, not purpose-oriented
- 의존성을 추가했습니다                 // Past tense
```

## JIRA Ticket Rules

### Correct Format

```text
Ref: #S14P31A209-36
Ref: #S14P31A209-36, #S14P31A209-45
```

### Incorrect Format

```text
Ref. #S14P31A209-36             // Use colon, not period
Ref S14P31A209-36               // Missing : and # symbol
#S14P31A209-36                  // Missing "Ref:" prefix
S14P31A209-36                   // Missing both "Ref:" and #
```

### Requirements

- **Must start with** `Ref: #`
- **Separate multiple tickets** with `, #`
- **Include accurate** JIRA issue number
- **Add empty line** before this section

## Logical Commit Splitting

커밋은 파일 개수가 아니라 논리적 변경 단위로 나눕니다.

분리 기준:

- 같은 목적을 해결하는 변경인가?
- 되돌릴 때 같이 되돌리는 것이 자연스러운가?
- 리뷰어가 한 관점으로 검토할 수 있는가?
- 설정, 공통 인프라, 도메인 예제, 문서가 불필요하게 섞이지 않았는가?

좋은 제목은 파일명이 아니라 코드 묶음의 의미를 압축합니다.

```text
Good: 🔧 config: 코드 품질 검사 구성
Bad:  🔧 config: build.gradle 수정

Good: ✨ feat: 공통 응답 모델 구성
Bad:  ✨ feat: response 파일 추가
```

## Quick Checklist

Before committing, verify:

- [ ] Unicode gitmoji used (not text code)
- [ ] Type is lowercase
- [ ] Subject is under 50 characters
- [ ] Subject uses imperative or noun style
- [ ] No ticket number in subject
- [ ] No period at end of subject
- [ ] Empty line after subject
- [ ] Body uses `-` for bullet points
- [ ] JIRA ticket format: `Ref: #TICKET-NUMBER`
- [ ] Empty line before JIRA ticket reference
- [ ] Commit is split by logical purpose, not by file type alone
