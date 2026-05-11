package core.repository;

import core.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    // 정산 배치용: 특정 기간 SUCCESS 결제를 memberId 기준으로 합산
    @Query("""
        SELECT p.member.id, SUM(p.amount)
        FROM Payment p
        WHERE p.status = 'SUCCESS'
          AND p.createdAt >= :from
          AND p.createdAt < :to
        GROUP BY p.member.id
    """)
    List<Object[]> sumAmountGroupByMember(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
