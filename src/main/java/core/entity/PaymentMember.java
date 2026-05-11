package core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Long balance = 0L;

    @Version
    private Long version; // 낙관적 락

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static PaymentMember create(String email) {
        PaymentMember m = new PaymentMember();
        m.email = email;
        m.balance = 0L;
        return m;
    }

    public void deduct(Long amount) {
        if (this.balance < amount) {
            throw new IllegalStateException("잔액 부족");
        }
        this.balance -= amount;
    }

    public void charge(Long amount) {
        this.balance += amount;
    }
}
