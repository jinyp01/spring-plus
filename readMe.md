# Spring Plus 프로젝트 README

## 프로젝트 소개

Spring Plus는 일정(Todo)을 생성하고, 댓글과 담당자를 관리할 수 있는 Spring Boot 기반 백엔드 API 프로젝트입니다.
JWT 기반 인증과 Spring Security 권한 처리를 적용했으며, Querydsl을 활용한 일정 검색과 대용량 유저 검색 성능 테스트 기능이 포함되어 있습니다.

## 기술 스택

* Java 17
* Spring Boot 3.3.3
* Spring Web
* Spring Data JPA
* Spring Security
* Querydsl 5.0.0
* JWT
* BCrypt
* H2 Database
* MySQL Driver
* Gradle

## 주요 구현 내용

### 1. 인증 / 인가

* 회원가입 API 구현
* 로그인 API 구현
* JWT 토큰 발급
* JWT 필터를 통한 인증 처리
* Spring Security 기반 접근 제어 적용
* `/auth/**` 경로는 인증 없이 접근 가능
* `/admin/**` 경로는 `ADMIN` 권한만 접근 가능
* 그 외 API는 인증된 사용자만 접근 가능

### 2. 유저 기능

* 유저 단건 조회
* 비밀번호 변경
* 유저 권한 변경
* username 기준 유저 검색
* `users.username` 컬럼 인덱스 설정

### 3. 일정(Todo) 기능

* 일정 생성
* 일정 단건 조회
* 일정 목록 조회
* 날씨 조건 및 수정일 범위 기반 일정 목록 조회
* Querydsl 기반 일정 검색

    * 제목 검색
    * 생성일 범위 검색
    * 담당자 닉네임 검색
    * 댓글 수 조회
    * 담당자 수 조회

### 4. 댓글 기능

* 일정에 댓글 등록
* 일정별 댓글 목록 조회
* 댓글 조회 시 작성자 정보 함께 반환

### 5. 담당자 기능

* 일정 담당자 등록
* 일정 담당자 목록 조회
* 일정 담당자 삭제
* 일정 작성자만 담당자 등록/삭제 가능
* 일정 작성자는 자기 자신을 담당자로 등록할 수 없도록 검증

### 6. 로그 기능

* 담당자 등록 요청 시 로그 저장
* 로그 저장은 `REQUIRES_NEW` 트랜잭션으로 분리
* 관리자 권한 변경 API 호출 시 AOP 기반 접근 로그 출력

### 7. 외부 API 연동

* 일정 생성 시 외부 날씨 API를 호출하여 오늘의 날씨 정보를 저장
* 호출 대상: `https://f-api.github.io/f-api/weather.json`

### 8. 예외 처리

* `InvalidRequestException` 발생 시 400 응답
* `AuthException` 발생 시 401 응답
* `ServerException` 발생 시 500 응답
* 공통 에러 응답 형식 제공

### 9. 성능 테스트

* `@Tag("bulk")` 기반 대용량 테스트 분리
* 일반 테스트 실행 시 bulk 테스트 제외
* `bulkUserTest` Gradle 태스크로 대용량 테스트만 별도 실행
* JDBC Batch Insert로 유저 100만 건 생성
* username 검색 성능 비교

    * 인덱스 없는 조회
    * 인덱스 적용 후 `SELECT *` 조회
    * 인덱스 적용 후 필요한 컬럼만 조회

## 주요 API

| 기능        | Method | URL                                    |
| --------- | -----: | -------------------------------------- |
| 회원가입      |   POST | `/auth/signup`                         |
| 로그인       |   POST | `/auth/signin`                         |
| 유저 조회     |    GET | `/users/{userId}`                      |
| 유저 검색     |    GET | `/users/search?nickname={nickname}`    |
| 비밀번호 변경   |    PUT | `/users`                               |
| 유저 권한 변경  |  PATCH | `/admin/users/{userId}`                |
| 일정 생성     |   POST | `/todos`                               |
| 일정 목록 조회  |    GET | `/todos`                               |
| 일정 검색     |    GET | `/todos/search`                        |
| 일정 단건 조회  |    GET | `/todos/{todoId}`                      |
| 댓글 등록     |   POST | `/todos/{todoId}/comments`             |
| 댓글 목록 조회  |    GET | `/todos/{todoId}/comments`             |
| 담당자 등록    |   POST | `/todos/{todoId}/managers`             |
| 담당자 목록 조회 |    GET | `/todos/{todoId}/managers`             |
| 담당자 삭제    | DELETE | `/todos/{todoId}/managers/{managerId}` |

## 실행 시 필요한 설정

`application.yml`에서 JWT Secret Key를 환경변수로 주입받습니다.

```yml
jwt:
  secret:
    key: ${JWT_SECRET_KEY}
```

실행 전 `JWT_SECRET_KEY` 환경변수를 설정해야 합니다.

## 테스트 실행

일반 테스트 실행:

```bash
./gradlew test
```

대용량 유저 검색 성능 테스트 실행:

```bash
./gradlew bulkUserTest
```

## 프로젝트 구조

```text
org.example.expert
├── aop
├── client
├── config
└── domain
    ├── auth
    ├── comment
    ├── common
    ├── log
    ├── manager
    ├── todo
    └── user
```

## 정리

이 프로젝트는 JWT 인증 기반의 일정 관리 API를 중심으로, 댓글/담당자/유저 권한 관리 기능을 구현한 백엔드 프로젝트입니다.
추가로 Querydsl 검색, Spring Security 권한 제어, AOP 로그, 별도 트랜잭션 로그 저장, JDBC 기반 대용량 데이터 성능 테스트까지 포함되어 있습니다.
