package core.service;

import core.dto.PaymentRequestDto;
import core.entity.Payment;
import core.entity.PaymentMember;
import core.global.lock.DistributedLock;
import core.repository.PaymentMemberRepository;
import core.repository.PaymentRepository;
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

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final long IDEMPOTENCY_TTL_MINUTES = 10;

    public Payment pay(PaymentRequestDto request) {
        // 1. 멱등키 체크 (Redis) — 중복 요청 차단
        String redisKey = IDEMPOTENCY_PREFIX + request.getIdempotencyKey();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "processing", Duration.ofMinutes(IDEMPOTENCY_TTL_MINUTES));

        if (Boolean.FALSE.equals(isNew)) {
            // 이미 처리된 요청 → DB에서 기존 결과 반환
            return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() -> new IllegalStateException("중복 요청 처리 중입니다. 잠시 후 다시 시도해주세요."));
        }

        // 2. 분산락으로 동시 결제 요청 직렬화
        String lockKey = "payment:member:" + request.getMemberId();
        return distributedLock.execute(lockKey, 3, 5, () -> processPayment(request));
    }

    @Transactional
    protected Payment processPayment(PaymentRequestDto request) {
        PaymentMember member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Payment payment = Payment.create(request.getIdempotencyKey(), member, request.getAmount());
        paymentRepository.save(payment);

        try {
            member.deduct(request.getAmount()); // 낙관적 락 적용됨 (@Version)
            payment.success();
        } catch (Exception e) {
            payment.fail();
            log.error("결제 실패 memberId={}, amount={}, error={}", request.getMemberId(), request.getAmount(), e.getMessage());
            throw e;
        }

        return payment;
    }
}
