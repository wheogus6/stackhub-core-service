package core.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    private String idempotencyKey;
    private Long memberId;
    private Long amount;
}
