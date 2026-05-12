package core.domain.settlement.batch;


import core.domain.member.repository.PaymentMemberRepository;
import core.domain.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * SettlementItem → Settlement 엔티티 변환
 * 회원 조회 실패 시 null 반환 → Writer에서 skip
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementProcessor implements ItemProcessor<SettlementItem, Settlement> {

    private final PaymentMemberRepository memberRepository;

    @Override
    public Settlement process(SettlementItem item) {
        return memberRepository.findById(item.getMemberId())
                .map(member -> {
                    Settlement settlement = Settlement.create(
                            member,
                            item.getTotalAmount(),
                            LocalDate.now().minusDays(1) // 어제 기준
                    );
                    log.info("[정산 배치] 처리 memberId={}, amount={}", member.getId(), item.getTotalAmount());
                    return settlement;
                })
                .orElseGet(() -> {
                    log.warn("[정산 배치] 회원 없음 memberId={}", item.getMemberId());
                    return null; // null → Writer skip
                });
    }
}
