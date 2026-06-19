# Spring Plus

Spring Boot 기반의 일정 관리 API에 JWT 인증, 권한 관리, Querydsl 검색, Redis Pub/Sub 기반 실시간 채팅을 더한 예제 프로젝트입니다.

## 기술 스택

- Java 17
- Spring Boot 3.3.3
- Spring Web, Spring Security, Spring Data JPA
- Spring WebSocket, STOMP, SockJS
- Spring Data Redis
- Querydsl 5
- JWT, BCrypt
- H2, MySQL Driver
- Gradle

## 주요 기능

- 회원가입, 로그인, JWT 기반 인증
- 사용자 조회, 닉네임 검색, 비밀번호 변경
- 관리자 전용 사용자 권한 변경
- 일정 생성, 단건 조회, 페이징 목록 조회
- 일정 제목, 생성일, 담당자 닉네임 기반 복합 검색
- 댓글 등록 및 조회
- 일정 담당자 등록, 조회, 삭제
- 채팅방 생성, 목록 조회, 메시지 조회
- WebSocket/STOMP 기반 채팅 메시지 송신
- Redis Pub/Sub을 이용한 채팅 메시지 발행 및 구독
- 사용자 닉네임 검색 성능 비교용 대량 데이터 테스트

## 프로젝트 구조

```text
src/main/java/org/example/expert
├── aop
├── client
├── config
└── domain
    ├── auth
    ├── chat
    ├── comment
    ├── common
    ├── log
    ├── manager
    ├── todo
    └── user
```

## 실행 환경

### 필수 요구사항

- JDK 17
- Redis 6379 포트 실행

### 환경 변수

`application.yml`은 다음 환경 변수를 사용합니다.

| 이름 | 필수 여부 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `JWT_SECRET_KEY` | 필수 | 없음 | JWT 서명에 사용할 Base64 인코딩 키 |
| `REDIS_HOST` | 선택 | `localhost` | Redis 호스트 |
| `REDIS_PORT` | 선택 | `6379` | Redis 포트 |

`JWT_SECRET_KEY`는 HS256 서명에 사용할 수 있도록 충분히 긴 Base64 문자열이어야 합니다.

PowerShell 예시:

```powershell
$env:JWT_SECRET_KEY="Base64로_인코딩된_시크릿_키"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
```

## 실행 방법

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS 또는 Linux:

```bash
./gradlew bootRun
```

기본 포트는 Spring Boot 기본값인 `8080`입니다.

## 인증

`/auth/**`와 `/ws/**`를 제외한 API는 JWT 인증이 필요합니다.

로그인 또는 회원가입 응답으로 받은 토큰을 `Authorization` 헤더에 넣어 호출합니다.

```http
Authorization: Bearer {token}
```

관리자 API인 `/admin/**`는 `ADMIN` 권한이 필요합니다.

## REST API

### Auth

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/auth/signup` | 회원가입 | 불필요 |
| `POST` | `/auth/signin` | 로그인 | 불필요 |

회원가입 요청 예시:

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "username": "nickname",
  "userRole": "USER"
}
```

로그인 요청 예시:

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

### User

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `GET` | `/users/{userId}` | 사용자 단건 조회 | 필요 |
| `GET` | `/users/search?nickname={nickname}` | 닉네임 기반 사용자 검색 | 필요 |
| `PUT` | `/users` | 비밀번호 변경 | 필요 |

비밀번호 변경 요청 예시:

```json
{
  "oldPassword": "password1234",
  "newPassword": "newPassword1234"
}
```

### Admin

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `PATCH` | `/admin/users/{userId}` | 사용자 권한 변경 | `ADMIN` 필요 |

권한 변경 요청 예시:

```json
{
  "role": "ADMIN"
}
```

### Todo

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/todos` | 일정 생성 | 필요 |
| `GET` | `/todos` | 일정 목록 조회 | 필요 |
| `GET` | `/todos/search` | 일정 검색 | 필요 |
| `GET` | `/todos/{todoId}` | 일정 단건 조회 | 필요 |

일정 생성 요청 예시:

```json
{
  "title": "할 일 제목",
  "contents": "할 일 내용"
}
```

목록 조회 쿼리 파라미터:

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| `page` | `1` | 페이지 번호 |
| `size` | `10` | 페이지 크기 |
| `weather` | 없음 | 날씨 필터 |
| `modifiedFrom` | 없음 | 수정일 시작, ISO DateTime |
| `modifiedTo` | 없음 | 수정일 종료, ISO DateTime |

검색 쿼리 파라미터:

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| `page` | `1` | 페이지 번호 |
| `size` | `10` | 페이지 크기 |
| `title` | 없음 | 제목 검색어 |
| `createdFrom` | 없음 | 생성일 시작, ISO DateTime |
| `createdTo` | 없음 | 생성일 종료, ISO DateTime |
| `managerNickname` | 없음 | 담당자 닉네임 |

### Comment

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/todos/{todoId}/comments` | 댓글 등록 | 필요 |
| `GET` | `/todos/{todoId}/comments` | 댓글 목록 조회 | 필요 |

댓글 등록 요청 예시:

```json
{
  "contents": "댓글 내용"
}
```

### Manager

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/todos/{todoId}/managers` | 일정 담당자 등록 | 필요 |
| `GET` | `/todos/{todoId}/managers` | 일정 담당자 목록 조회 | 필요 |
| `DELETE` | `/todos/{todoId}/managers/{managerId}` | 일정 담당자 삭제 | 필요 |

담당자 등록 요청 예시:

```json
{
  "managerUserId": 2
}
```

### Chat

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| `POST` | `/chat/rooms` | 채팅방 생성 | 필요 |
| `GET` | `/chat/rooms` | 채팅방 목록 조회 | 필요 |
| `GET` | `/chat/rooms/{roomId}/messages?size=50` | 채팅 메시지 조회 | 필요 |

채팅방 생성 요청 예시:

```json
{
  "name": "프로젝트 채팅방"
}
```

## WebSocket

STOMP 엔드포인트:

```text
/ws
```

연결 시 `Authorization` native header에 JWT를 전달해야 합니다.

```text
Authorization: Bearer {token}
```

메시지 송신:

```text
/pub/chat/rooms/{roomId}/messages
```

메시지 요청 바디:

```json
{
  "content": "안녕하세요"
}
```

구독 경로:

```text
/sub/chat/rooms/{roomId}
```

## 테스트

일반 테스트:

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```

## 사용자 검색 성능 테스트

기본 `test` task에서는 100만 건 대량 데이터 테스트를 제외합니다. 별도 task로 실행할 수 있습니다.

```bash
./gradlew bulkUserTest
```

Windows:

```powershell
.\gradlew.bat bulkUserTest
```

성능 테스트는 H2 인메모리 DB에 사용자 1,000,000건을 `JdbcTemplate.batchUpdate`로 삽입한 뒤 다음 조건의 평균 조회 시간을 비교합니다.

| 단계 | 조회 방식 |
| --- | --- |
| 전체 스캔 | `SELECT * FROM users WHERE username = ?` |
| 인덱스 적용 | `idx_users_username` 생성 후 `SELECT *` 조회 |
| 프로젝션 적용 | 인덱스 적용 후 `id`, `email`, `username`만 조회 |

닉네임은 `nick_{index}_{randomBase36}` 형식으로 생성되며, 중간 지점의 사용자를 검색 대상으로 사용합니다.

## 참고 사항

- 기본 설정에는 명시적인 datasource가 없으므로 로컬 실행 시 H2 인메모리 DB가 사용됩니다.
- MySQL을 사용하려면 `spring.datasource.*` 설정을 별도로 추가해야 합니다.
- 실시간 채팅 발행과 구독에는 Redis 연결이 필요합니다.
- JWT 토큰 만료 시간은 `JwtUtil` 기준 60분입니다.
