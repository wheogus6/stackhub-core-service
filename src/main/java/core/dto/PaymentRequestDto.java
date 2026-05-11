package core.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    private String idempotencyKey; // 클라이언트가 생성한 UUID
    private Long memberId;
    private Long amount;
}
