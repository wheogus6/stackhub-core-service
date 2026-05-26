package core.domain.payment.event.consumer;

import core.domain.payment.event.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트 Kafka 컨슈머
 *
 * 실제 운영에서는 이 컨슈머 대신 알림 서비스, 로그 서비스 등
 * 별도 MSA 서비스가 payment.completed 토픽을 구독합니다.
 */
@Slf4j
@Component
public class PaymentEventConsumer {

    /**
     * 결제 완료 이벤트 수신
     * - 실제 서비스라면 알림 발송, 포인트 적립 등의 후처리 로직이 들어옴
     */
    @KafkaListener(
            topics = "payment.completed",
            groupId = "payment-core-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentCompleted(PaymentEvent event) {
        log.info("[Kafka Consumer] 결제 완료 이벤트 수신 " +
                        "paymentId={}, memberId={}, amount={}, status={}, occurredAt={}",
                event.getPaymentId(),
                event.getMemberId(),
                event.getAmount(),
                event.getStatus(),
                event.getOccurredAt()
        );

        // 실제 서비스라면 여기에 후처리 로직 추가
    }

    /**
     * 결제 실패 이벤트 수신
     * - 실제 서비스라면 실패 알림, 재시도 큐 적재 등의 로직이 들어옴
     */
    @KafkaListener(
            topics = "payment.failed",
            groupId = "payment-core-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(PaymentEvent event) {
        log.warn("[Kafka Consumer] 결제 실패 이벤트 수신 " +
                        "paymentId={}, memberId={}, amount={}, occurredAt={}",
                event.getPaymentId(),
                event.getMemberId(),
                event.getAmount(),
                event.getOccurredAt()
        );

        // 실제 서비스라면 여기에 실패 처리 로직 추가
    }
}
