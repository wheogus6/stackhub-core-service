package core.domain.settlement.batch;

import core.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 어제 하루치 SUCCESS 결제를 memberId 기준으로 집계해서 읽어옴
 *
 * @StepScope: Step 실행마다 새 인스턴스 생성 → 상태(queue) 자동 초기화
 * 이전에는 @Component + reset() 수동 호출 방식이었으나,
 * 재시도/병렬 실행 시 상태 꼬임 가능성을 제거하기 위해 @StepScope로 변경
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SettlementReader implements ItemReader<SettlementItem> {

    private final PaymentRepository paymentRepository;
    private Queue<SettlementItem> queue;

    @Override
    public SettlementItem read() {
        if (queue == null) {
            queue = loadItems();
        }
        return queue.poll(); // null 반환 시 배치 종료
    }

    private Queue<SettlementItem> loadItems() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime from = yesterday.atStartOfDay();
        LocalDateTime to = yesterday.plusDays(1).atStartOfDay();

        List<Object[]> rows = paymentRepository.sumAmountGroupByMember(from, to);
        log.info("[정산 배치] 대상 회원 수: {}", rows.size());

        Queue<SettlementItem> q = new LinkedList<>();
        for (Object[] row : rows) {
            Long memberId = (Long) row[0];
            Long totalAmount = (Long) row[1];
            q.add(new SettlementItem(memberId, totalAmount));
        }
        return q;
    }
}
