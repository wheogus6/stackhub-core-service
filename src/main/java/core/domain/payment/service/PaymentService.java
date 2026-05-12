package core.domain.payment.service;

import core.domain.member.entity.PaymentMember;
import core.domain.member.repository.PaymentMemberRepository;
import core.domain.payment.dto.PaymentRequestDto;
import core.domain.payment.entity.Payment;
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

    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final long IDEMPOTENCY_TTL_MINUTES = 10;

    @Transactional
    public Payment pay(PaymentRequestDto request) {
        // 1. 멱등키 체크 (Redis) — 중복 요청 차단
        String redisKey = IDEMPOTENCY_PREFIX + request.getIdempotencyKey();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "processing", Duration.ofMinutes(IDEMPOTENCY_TTL_MINUTES));

        if (Boolean.FALSE.equals(isNew)) {
            // 이미 처리된 요청 → DB에서 기존 결과 반환
            return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(PaymentException.DuplicateRequestException::new);
        }

        // 2. 분산락으로 동시 결제 요청 직렬화
        String lockKey = "payment:member:" + request.getMemberId();
        return distributedLock.execute(lockKey, 3, 5, () -> processPayment(request));
    }

    protected Payment processPayment(PaymentRequestDto request) {
        PaymentMember member = memberRepository.findById(request.getMemberId())
                .orElseThrow(MemberException.NotFoundException::new);

        Payment payment = Payment.create(request.getIdempotencyKey(), member, request.getAmount());
        paymentRepository.save(payment);

        try {
            member.deduct(request.getAmount()); // 낙관적 락 적용됨 (@Version)
            payment.success();
        } catch (IllegalStateException e) {
            payment.fail();
            log.error("결제 실패 memberId={}, amount={}", request.getMemberId(), request.getAmount());
            throw new PaymentException.BalanceNotEnoughException();
        } catch (Exception e) {
            payment.fail();
            log.error("결제 실패 memberId={}, amount={}, error={}", request.getMemberId(), request.getAmount(), e.getMessage());
            throw e;
        }

        return payment;
    }
}
