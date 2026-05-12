package core.domain.payment.entity;

import core.domain.member.entity.PaymentMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey; // 멱등키 (중복 결제 방지)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private PaymentMember member;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, CANCELLED
    }

    public static Payment create(String idempotencyKey, PaymentMember member, Long amount) {
        Payment p = new Payment();
        p.idempotencyKey = idempotencyKey;
        p.member = member;
        p.amount = amount;
        p.status = PaymentStatus.PENDING;
        return p;
    }

    public void success() { this.status = PaymentStatus.SUCCESS; }
    public void fail()    { this.status = PaymentStatus.FAILED; }
    public void cancel()  { this.status = PaymentStatus.CANCELLED; }
}
