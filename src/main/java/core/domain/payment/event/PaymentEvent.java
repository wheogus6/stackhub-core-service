package core.domain.payment.event;

import core.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 완료 이벤트 DTO
 * Kafka topic: payment.completed
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private Long paymentId;
    private Long memberId;
    private Long amount;
    private String status;
    private LocalDateTime occurredAt;

    public static PaymentEvent of(Payment payment) {
        return new PaymentEvent(
                payment.getId(),
                payment.getMember().getId(),
                payment.getAmount(),
                payment.getStatus().name(),
                LocalDateTime.now()
        );
    }
}
