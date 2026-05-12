package core.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChargeRequestDto {
    private Long memberId;
    private Long amount;
}
