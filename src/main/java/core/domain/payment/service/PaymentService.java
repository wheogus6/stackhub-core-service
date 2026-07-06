package core.domain.payment.service;

import core.domain.member.entity.PaymentMember;
import core.domain.member.repository.PaymentMemberRepository;
import core.domain.payment.dto.PaymentRequestDto;
import core.domain.payment.entity.Payment;
import core.domain.payment.event.PaymentEvent;
import core.domain.payment.event.PaymentEventProducer;
import core.domain.payment.repository.PaymentRepository;
import core.global.exception.MemberException;
import core.global.exception.PaymentException;
import core.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;
    private final DistributedLock distributedLock;
    private final PaymentEventProducer paymentEventProducer;

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final long IDEMPOTENCY_TTL_MINUTES = 10;

    @Transactional
    public Payment pay(PaymentRequestDto request) {
        // 1. 멱등키 체크 — 중복/재시도 요청 조기 차단
        String redisKey = IDEMPOTENCY_PREFIX + request.getIdempotencyKey();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(
                        redisKey,
                        "processing",
                        Duration.ofMinutes(IDEMPOTENCY_TTL_MINUTES));

        if (Boolean.FALSE.equals(isNew)) {
            // 이미 처리된 요청 → DB에서 기존 결과 반환
            return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(PaymentException.DuplicateRequestException::new);
        }

        // 2. 분산락으로 동시 결제 요청 직렬화
        String lockKey = "payment:member:" + request.getMemberId();
        try {
            return distributedLock.execute(
                    lockKey,
                    3,
                    5,
                    () -> processPayment(request));
        } catch (Exception e) {
            // 결제 실패 시 멱등키 삭제 → 클라이언트가 동일 키로 재시도 가능
            redisTemplate.delete(redisKey);
            log.warn("[멱등키 삭제] 결제 실패로 멱등키 제거 key={}", redisKey);
            throw e;
        }
    }

    /**
     * 실제 결제 처리 로직
     * 분산락 내부에서 실행되며, @Transactional은 pay()에서 이미 시작된 트랜잭션을 이어받음
     */
    private Payment processPayment(PaymentRequestDto request) {
        PaymentMember member = memberRepository.findById(request.getMemberId())
                .orElseThrow(MemberException.NotFoundException::new);

        Payment payment = Payment.create(
                request.getIdempotencyKey(),
                member, request.getAmount());

        paymentRepository.save(payment);

        try {
            // 낙관적 락 적용됨 (@Version)
            member.deduct(request.getAmount());
            payment.success();
            // 결제 완료 이벤트 발행
            paymentEventProducer.sendPaymentCompleted(PaymentEvent.of(payment));
        } catch (IllegalStateException e) {
            payment.fail();
            log.error("결제 실패 memberId={}, amount={}",
                    request.getMemberId(),
                    request.getAmount()
            );
            // 결제 실패 이벤트 발행
            paymentEventProducer.sendPaymentFailed(PaymentEvent.of(payment));
            throw new PaymentException.BalanceNotEnoughException();
        } catch (Exception e) {
            payment.fail();
            log.error("결제 실패 memberId={}, amount={}, error={}",
                    request.getMemberId(),
                    request.getAmount(),
                    e.getMessage()
            );
            // 결제 실패 이벤트 발행
            paymentEventProducer.sendPaymentFailed(PaymentEvent.of(payment));
            throw e;
        }
        return payment;
    }
}
