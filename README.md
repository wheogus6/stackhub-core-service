# StackHub Core Service

> 결제 및 정산 시스템 백엔드 서비스  
> 트랜잭션 정합성, 동시성 제어, 대용량 배치 처리, 이벤트 드리븐 아키텍처에 집중한 포트폴리오 프로젝트

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| ORM | Spring Data JPA |
| DB | PostgreSQL |
| Cache / Lock | Redis, Redisson |
| Batch | Spring Batch |
| Message Broker | Apache Kafka |
| API 문서 | Swagger (SpringDoc OpenAPI 2.8) |
| Build | Gradle |

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
      event/        PaymentEvent, PaymentEventProducer
        consumer/   PaymentEventConsumer
      repository/
      service/      PaymentService, ChargeService
    settlement/
      batch/        Reader(@StepScope), Processor, Writer, Scheduler
      entity/       Settlement
      repository/
  global/
    config/         Batch, JpaAuditing, Swagger, Kafka 설정
    exception/      CustomException, GlobalExceptionHandler
    jdbc/           SettlementJdbcSqlSpec (TEMP 테이블 + MERGE SQL)
    lock/           DistributedLock (Redisson)
```

---

## 시스템 아키텍처

```
클라이언트
    │
    ▼
PaymentController
    │
    ▼
PaymentService
    ├─ [1차 방어] Redis 멱등키 (SET NX)
    │       중복 요청 → DB에서 기존 결과 반환
    │       결제 실패 → 멱등키 삭제 (재시도 허용)
    │
    └─ [2차 방어] Redisson 분산락
            │
            ▼
        processPayment()
            ├─ PaymentMember.deduct()  ← @Version 낙관적 락
            ├─ Payment.success() / fail()
            └─ KafkaProducer
                    ├─ payment.completed → (MSA: 알림/포인트 서비스)
                    └─ payment.failed   → (MSA: 실패 알림/재시도 큐)

정산 배치 (매일 02:00)
    SettlementScheduler
        └─ SettlementJob
              ├─ SettlementReader   (@StepScope) — 전날 SUCCESS 결제 집계
              ├─ SettlementProcessor             — Settlement 엔티티 변환
              └─ SettlementWriter (JDBC)         — TEMP + batchUpdate + MERGE
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

### 2. 멱등성 처리 — 중복 결제 방지 + 실패 재시도 허용

```java
Boolean isNew = redisTemplate.opsForValue()
    .setIfAbsent(redisKey, "processing", Duration.ofMinutes(10));

if (Boolean.FALSE.equals(isNew)) {
    // 이미 처리된 요청 → DB에서 기존 결과 반환
    return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
            .orElseThrow(PaymentException.DuplicateRequestException::new);
}

// 결제 실패 시 멱등키 삭제 → 동일 키로 재시도 가능
try {
    return distributedLock.execute(...);
} catch (Exception e) {
    redisTemplate.delete(redisKey);
    throw e;
}
```

클라이언트가 UUID 기반 멱등키를 생성해서 요청합니다. Redis `SET NX` 명령어로 원자적으로 중복 여부를 체크하고, 이미 처리된 요청이면 DB에서 기존 결과를 반환합니다.

**트레이드오프 인식**
- 결제 성공 시: 멱등키가 TTL(10분)까지 유지되어 재요청을 차단
- 결제 실패 시: 멱등키를 즉시 삭제하여 동일 키로 재시도 허용
- Redis 장애 시: `setIfAbsent`가 null 반환 → 멱등성 보장 불가. 이 경우 DB의 `idempotencyKey unique 제약`이 최후 방어선으로 작동

> DB 대신 Redis에 저장한 이유: 빠른 조회 + TTL 자동 만료로 별도 관리가 필요 없기 때문입니다.

---

### 3. Kafka 이벤트 드리븐 — 결제 이벤트 발행/구독

결제 완료/실패 시 Kafka 토픽으로 이벤트를 발행합니다. 이를 통해 결제 로직과 후처리 로직(알림, 로그 등)을 느슨하게 분리합니다.

**이벤트 흐름**

```
결제 요청
  → PaymentService
      → 성공 → PaymentEventProducer → payment.completed 토픽
      → 실패 → PaymentEventProducer → payment.failed 토픽
                                            ↓
                                    PaymentEventConsumer 수신
                                    (실제 운영: 알림/로그 서비스가 구독)
```

**Producer — memberId를 파티션 키로 사용해 같은 회원의 이벤트 순서 보장**

```java
kafkaTemplate.send(TOPIC_PAYMENT_COMPLETED, event.getMemberId().toString(), event);
```

**Kafka 토픽 구성**

| 토픽 | groupId | 설명 |
|------|---------|------|
| payment.completed | payment-core-group | 결제 성공 이벤트 |
| payment.failed | payment-core-group | 결제 실패 이벤트 |

> 현재 Consumer는 단일 서비스 내 구현으로, 실제 MSA 환경에서는 알림 서비스 / 로그 서비스 등 별도 서비스가 토픽을 구독하는 구조로 확장됩니다.

---

### 4. 정산 배치 — 대용량 JDBC 처리

JPA `saveAll()` 대신 TEMP 테이블 + `batchUpdate` + MERGE(upsert) 패턴을 적용했습니다.

```
JPA saveAll(N건)      → N번 INSERT (엔티티 오버헤드 포함)
JDBC TEMP + MERGE     → 1번 batchUpdate + 1번 MERGE 쿼리
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

**`@StepScope` 적용으로 상태 안전성 보장**

`SettlementReader`는 내부에 Queue 상태를 가지므로 `@StepScope`를 적용했습니다. Step 실행마다 새 인스턴스가 생성되어 재실행/병렬 실행 시 상태 꼬임이 없습니다.

**배치 구성**

| 항목 | 내용 |
|------|------|
| 실행 주기 | 매일 새벽 02:00 (Cron) |
| 청크 단위 | 100건 |
| 실패 처리 | 개별 건 skip (skipLimit 10건) |
| 대상 데이터 | 전날 SUCCESS 결제를 memberId 기준 집계 |

---

### 5. 예외 처리 — 도메인별 커스텀 예외

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

## 테스트

```bash
./gradlew test
```

| 테스트 클래스 | 검증 내용 |
|---|---|
| `PaymentServiceTest` | 정상 결제, 멱등키 중복 차단, 실패 시 멱등키 삭제, 잔액 부족, 회원 없음 |
| `SettlementWriterTest` | 빈 청크 무시, TEMP→batchUpdate→MERGE 실행 순서, 수수료 계산 |

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
- Apache Kafka

**설정**

`src/main/resources/application.properties`에서 DB, Redis, Kafka 연결 정보를 설정합니다.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=
spring.datasource.password=

spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.kafka.bootstrap-servers=localhost:9092
```

**Kafka 로컬 실행 (Docker)**

```yaml
# docker-compose.yml
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
```

**빌드 및 실행**

```bash
./gradlew bootRun
```

---

## 관련 프로젝트

- [stackhub-auth-service](https://github.com/wheogus6/stackhub-auth-service) — 인증/인가 서비스 (JWT 발급)

> 인증은 auth 서비스에서 담당하며, core 서비스는 비즈니스 로직에 집중합니다.
