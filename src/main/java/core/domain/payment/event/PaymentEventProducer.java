package core.domain.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 결제 이벤트 Kafka 프로듀서
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String TOPIC_PAYMENT_COMPLETED = "payment.completed";
    private static final String TOPIC_PAYMENT_FAILED = "payment.failed";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    /**
     * 결제 성공 이벤트 발행
     * key: memberId → 같은 회원의 이벤트는 같은 파티션으로 순서 보장
     */
    public void sendPaymentCompleted(PaymentEvent event) {
        kafkaTemplate.send(TOPIC_PAYMENT_COMPLETED, event.getMemberId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] 결제 완료 이벤트 발행 실패 paymentId={}, error={}",
                                event.getPaymentId(), ex.getMessage());
                    } else {
                        log.info("[Kafka] 결제 완료 이벤트 발행 성공 paymentId={}, topic={}, partition={}, offset={}",
                                event.getPaymentId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * 결제 실패 이벤트 발행
     */
    public void sendPaymentFailed(PaymentEvent event) {
        kafkaTemplate.send(TOPIC_PAYMENT_FAILED, event.getMemberId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] 결제 실패 이벤트 발행 실패 paymentId={}, error={}",
                                event.getPaymentId(), ex.getMessage());
                    } else {
                        log.info("[Kafka] 결제 실패 이벤트 발행 paymentId={}", event.getPaymentId());
                    }
                });
    }
}
