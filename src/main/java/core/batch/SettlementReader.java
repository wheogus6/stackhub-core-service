package core.batch;


import core.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 * 어제 하루치 SUCCESS 결제를 memberId 기준으로 집계해서 읽어옴
 */
@Slf4j
@Component
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

    // 배치 재실행 시 초기화 (Step scope 미사용 시 필요)
    public void reset() {
        this.queue = null;
    }
}
