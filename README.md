# StackHub Core Service

> 결제 및 정산 시스템 백엔드 서비스
> 트랜잭션 정합성, 동시성 제어, 대용량 배치 처리에 집중한 포트폴리오 프로젝트

---

## 기술 스택

| 분류 | 기술                              |
|------|---------------------------------|
| Language | Java 21                         |
| Framework | Spring Boot 3.4.1               |
| ORM | Spring Data JPA                 |
| DB | PostgreSQL                      |
| Cache / Lock | Redis, Redisson                 |
| Batch | Spring Batch                    |
| API 문서 | Swagger (SpringDoc OpenAPI 2.8) |
| Build | Gradle                          |

---

## 프로젝트 구조

```
core/
  domain/
    member/
      controller/   회원 등록, 잔액 조회
      dto/
      entity/       PaymentMember (낙관적 락 @Version)
      repository/
      service/
    payment/
      controller/   결제, 충전 API
      dto/
      entity/       Payment (멱등키)
      repository/
      service/      PaymentService, ChargeService
    settlement/
      batch/        Reader, Processor, Writer, Scheduler
      entity/       Settlement
      repository/
  global/
    config/         Batch, JpaAuditing, Swagger 설정
    exception/      CustomException, GlobalExceptionHandler
    jdbc/           SettlementJdbcSqlSpec (TEMP 테이블 + MERGE SQL)
    lock/           DistributedLock (Redisson)
```

---

## 핵심 구현 포인트

### 1. 결제 동시성 제어 — 이중 방어 구조

동시에 같은 회원의 결제 요청이 들어올 때 잔액이 마이너스가 되는 문제를 두 가지 방법으로 방어합니다.

**1차 방어: Redis 분산락 (Redisson)**

```java
String lockKey = "payment:member:" + request.getMemberId();
return distributedLock.execute(lockKey, 3, 5, () -> processPayment(request));
```

같은 회원의 요청을 Redis 락으로 직렬화합니다. 락 키를 `memberId` 기준으로 잡아 서로 다른 회원끼리는 블로킹이 없습니다.

**2차 방어: 낙관적 락 (@Version)**

```java
@Version
private Long version;
```

Redis 장애 시에도 DB 레벨에서 동시 수정을 차단합니다. JPA가 자동으로 `WHERE version = ?` 조건을 추가하며 충돌 시 `OptimisticLockException`을 발생시킵니다.

> 분산락만 쓰면 Redis 장애 시 동시성 보장 불가. 낙관적 락만 쓰면 충돌 시 예외로 재시도 필요. 둘을 함께 써서 안전성을 높였습니다.

---

### 2. 멱등성 처리 — 중복 결제 방지

```java
Boolean isNew = redisTemplate.opsForValue()
    .setIfAbsent(redisKey, "processing", Duration.ofMinutes(10));

if (Boolean.FALSE.equals(isNew)) {
    return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
            .orElseThrow(PaymentException.DuplicateRequestException::new);
}
```

클라이언트가 UUID 기반 멱등키를 생성해서 요청합니다. Redis `SET NX` 명령어로 원자적으로 중복 여부를 체크하고, 이미 처리된 요청이면 DB에서 기존 결과를 반환합니다. TTL 10분으로 자동 만료됩니다.

> DB 대신 Redis에 저장한 이유: 빠른 조회 + TTL 자동 만료로 별도 관리가 필요 없기 때문입니다.

---

### 3. 정산 배치 — 대용량 JDBC 처리

JPA `saveAll()` 대신 TEMP 테이블 + `batchUpdate` + MERGE(upsert) 패턴을 적용했습니다.

```
JPA saveAll(N건)
→ N번 INSERT (엔티티 오버헤드 포함)

JDBC TEMP + MERGE
→ 1번 batchUpdate (bulk INSERT)
→ 1번 MERGE 쿼리 (upsert)
```

```java
// 1. TEMP 테이블 생성 (트랜잭션 종료 시 자동 삭제)
jdbcTemplate.execute(CREATE_TEMP_TABLE);

// 2. bulk INSERT
jdbcTemplate.batchUpdate(BULK_INSERT_TEMP, setter);

// 3. MERGE → settlements (inserted/updated 카운트 반환)
jdbcTemplate.query(MERGE_TEMP_TO_MAIN, rs -> ...);
```

PostgreSQL `ON CONFLICT DO UPDATE`로 동일 회원/날짜 정산이 이미 있으면 UPDATE, 없으면 INSERT합니다.

**배치 구성**

| 항목 | 내용 |
|------|------|
| 실행 주기 | 매일 새벽 02:00 (Cron) |
| 청크 단위 | 100건 |
| 실패 처리 | 개별 건 skip (skipLimit 10건) |
| 대상 데이터 | 전날 SUCCESS 결제를 memberId 기준 집계 |

---

### 4. 예외 처리 — 도메인별 커스텀 예외

```java
// 사용
throw new PaymentException.BalanceNotEnoughException();
throw new MemberException.NotFoundException();

// GlobalExceptionHandler가 받아서 처리
{
  "code": "PM001",
  "message": "잔액이 부족합니다."
}
```

`ResponseCode` enum에 코드 + 메시지 + HTTP 상태를 한 곳에 정의하고, 도메인별 예외 클래스의 static inner class로 구성해 사용처에서 의미가 명확하게 드러나도록 했습니다.

---

## API 명세

서버 실행 후 Swagger UI에서 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

| Method | URL | 설명 |
|--------|-----|------|
| POST | /api/member | 회원 등록 |
| GET | /api/member/{id}/balance | 잔액 조회 |
| POST | /api/payment/charge | 잔액 충전 |
| POST | /api/payment | 결제 요청 |

---

## 실행 방법

**사전 요구사항**
- Java 21
- PostgreSQL
- Redis

**설정**

`src/main/resources/application.properties`에서 DB, Redis 연결 정보를 설정합니다.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=
spring.datasource.password=

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**빌드 및 실행**

```bash(..%2F..%2F..%2F..%2FDesktop%2Ffiles%2FREADME.md)
./gradlew bootRun
```

---

## 관련 프로젝트

- [stackhub-auth-service](https://github.com/wheogus6/stackhub-auth-service) — 인증/인가 서비스 (JWT 발급)

> 인증은 auth 서비스에서 담당하며, core 서비스는 비즈니스 로직에 집중합니다.
