package core.domain.settlement.batch;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Reader → Processor → Writer 사이에 흐르는 데이터 단위
 */
@Getter
@AllArgsConstructor
public class SettlementItem {
    private Long memberId;
    private Long totalAmount;
}
