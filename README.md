# Envio Auth

Spring Boot 기반 핵심 API 서버입니다. 이 저장소는 도메인 단위로 코드를 나누고, 공통 응답/예외/검증/로깅 규칙을 일관되게 적용합니다.

현재 코드 작성 예시는 `OAuth/src/main/java/io/envio/auth/domain/user`에서 확인할 수 있습니다. 해당 도메인은 예제 목적의 기준이며, 이후 실제 요구사항에 맞게 교체될 수 있습니다.

## 개발 환경

- Java 25
- Spring Boot 4.0.5
- PostgreSQL
- Redis
- Gradle

로컬 인프라는 루트에서 실행합니다.

```bash
docker compose up -d
```

환경 변수는 `.env.example`을 기준으로 `.env`에 정의합니다.

```env
POSTGRES_DBNAME=enviodb
POSTGRES_USERNAME=user
POSTGRES_PASSWORD=password
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=password
```

## 명령어

명령은 `OAuth` 디렉터리에서 실행합니다.

```bash
# 빌드
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test

# 테스트
./gradlew test

# Checkstyle
./gradlew checkstyleMain

# 단일 테스트
./gradlew test --tests "io.envio.auth.domain.user.converter.UserConverterTest"
```

## 도메인 구조

새 도메인은 아래 형태를 기본으로 작성합니다. 필요한 계층만 추가하되, 역할이 섞이지 않도록 분리합니다.

```text
io.envio.auth
├── common
│   ├── aop
│   ├── config
│   ├── entity
│   ├── error
│   │   └── exception
│   ├── response
│   └── util
└── domain
    └── <domain>
        ├── controller
        ├── converter
        ├── dto
        │   ├── request
        │   └── response
        ├── entity
        ├── exception
        ├── repository
        └── service
            ├── command
            ├── facade
            └── query
```

예제 도메인 구조는 아래 경로에서 확인할 수 있습니다.

```text
OAuth/src/main/java/io/envio/auth/domain/user
├── controller
│   └── UserController.java
├── converter
│   └── UserConverter.java
├── dto
│   ├── request
│   │   └── UserCreateReqDto.java
│   └── response
│       └── UserResDto.java
├── entity
│   └── User.java
├── exception
│   └── UserException.java
├── repository
│   └── UserRepository.java
└── service
    ├── command
    │   ├── UserCommandService.java
    │   └── UserCommandServiceImpl.java
    ├── facade
    │   ├── UserFacadeService.java
    │   └── UserFacadeServiceImpl.java
    └── query
        ├── UserQueryService.java
        └── UserQueryServiceImpl.java
```

## 작성 규칙

### Controller

- 요청 DTO 검증, PathVariable/RequestParam 처리, 응답 반환만 담당합니다.
- 비즈니스 흐름은 `facade` 서비스로 위임합니다.
- 응답은 `BaseResponse`와 `ResponseUtils`를 사용합니다.

### Facade Service

- 하나의 API 흐름을 조립하는 계층입니다.
- 여러 command/query 서비스 호출과 DTO 변환 흐름을 조합합니다.
- 단순 CRUD라도 Controller가 직접 command/query를 호출하지 않도록 합니다.

### Command Service

- 생성, 수정, 삭제 같은 쓰기 로직을 담당합니다.
- 중복 검증, 상태 변경, 저장 로직을 이 계층에 둡니다.
- 기본적으로 `@Transactional`을 사용합니다.

### Query Service

- 조회 로직만 담당합니다.
- 기본적으로 `@Transactional(readOnly = true)`를 사용합니다.
- 조회 실패 시 도메인 예외를 던집니다.

### Converter

- Entity와 DTO 사이의 변환만 담당합니다.
- 비즈니스 판단이나 저장 로직을 넣지 않습니다.

### DTO

- 요청/응답 DTO를 분리합니다.
- `record`와 `@Builder`를 사용합니다.
- 요청 DTO에는 `jakarta.validation` 제약 조건을 명시합니다.

### Entity

- JPA 엔티티는 `BaseEntity`를 상속합니다.
- 기본 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 제한합니다.
- 외부에서 필드를 직접 변경하지 않도록 `@Getter` 중심으로 작성합니다.

### Exception

- 도메인 예외는 `BusinessException`을 상속합니다.
- 도메인별 에러 코드는 `common/error/ErrorCode.java`에 추가합니다.

## 응답 패턴

```java
@PostMapping
public ResponseEntity<BaseResponse<UserResDto>> createUser(
    @Valid @RequestBody final UserCreateReqDto reqDto
) {
    UserResDto response = facadeService.createUser(reqDto);
    return ResponseUtils.created(response);
}
```

## 테스트 규칙

테스트에는 `@DisplayName`을 사용하고, 어떤 동작을 검증하는 테스트인지 한국어로 작성합니다.
띄어쓰기는 공백 대신 언더바(`_`)를 사용합니다.
테스트 본문은 `given`, `when`, `then` 흐름이 드러나도록 구분합니다.

```java
@DisplayName("사용자_변환기")
class UserConverterTest {

    @Test
    @DisplayName("사용자_생성_요청을_엔티티로_변환한다")
    void toEntity_mapsCreateRequest() {
        // given

        // when

        // then
    }
}
```

## Checkstyle

Checkstyle은 Naver Java Convention 기반 규칙을 사용합니다.

- 규칙 파일: `OAuth/config/checkstyle/envio_auth-checkstyle-rules.xml`
- Suppression 파일: `OAuth/config/checkstyle/envio_auth-checkstyle-suppressions.xml`

PR 전에는 아래 명령을 실행합니다.

```bash
./gradlew checkstyleMain
```

주의할 점:

- 들여쓰기는 탭을 사용합니다.
- 한 줄은 120자를 넘기지 않습니다.
- star import를 사용하지 않습니다.
- import 그룹과 순서를 지킵니다.
- 클래스, 메서드, 변수명은 명확한 lowerCamelCase/UpperCamelCase 규칙을 따릅니다.

## GitHub Actions

- `gradle-build.yml`: `main`, `develop` 대상 push/PR에서 테스트 제외 빌드를 실행합니다.
- `claude-code-review.yml`: `develop` 대상 PR이 최초 열릴 때만 Claude Code 리뷰를 실행합니다.

Claude Code 리뷰를 사용하려면 GitHub Repository Secret에 `ANTHROPIC_API_KEY`가 필요합니다.
