package core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "settlements",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_settlements_member_date",
        columnNames = {"member_id", "settled_date"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private PaymentMember member;

    @Column(nullable = false)
    private Long totalAmount;    // 정산 대상 총액

    @Column(nullable = false)
    private Long feeAmount;      // 수수료 (3%)

    @Column(nullable = false)
    private Long settledAmount;  // 실지급액

    @Column(nullable = false)
    private LocalDate settledDate; // 정산 기준일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum SettlementStatus {
        PENDING, COMPLETED, FAILED
    }

    public static Settlement create(PaymentMember member, Long totalAmount, LocalDate settledDate) {
        Settlement s = new Settlement();
        s.member = member;
        s.totalAmount = totalAmount;
        s.feeAmount = totalAmount * 3 / 100;
        s.settledAmount = totalAmount - s.feeAmount;
        s.settledDate = settledDate;
        s.status = SettlementStatus.PENDING;
        return s;
    }

    public void complete() { this.status = SettlementStatus.COMPLETED; }
    public void fail()     { this.status = SettlementStatus.FAILED; }
}
