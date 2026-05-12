package core.domain.payment.controller;

import core.domain.payment.dto.ChargeRequestDto;
import core.domain.payment.dto.PaymentRequestDto;
import core.domain.payment.entity.Payment;
import core.domain.payment.service.ChargeService;
import core.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ChargeService chargeService;

    @Operation(
            summary = "잔액 충전",
            description = "회원 잔액을 충전합니다. 충전 후 잔액을 반환합니다."
    )
    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody ChargeRequestDto request) {
        Long balance = chargeService.charge(request);
        return ResponseEntity.ok(balance);
    }

    @Operation(
            summary = "결제 요청",
            description = "멱등키 기반 중복 방지 + 분산락으로 동시성 제어"
    )
    @PostMapping
    public ResponseEntity<?> pay(@RequestBody PaymentRequestDto request) {
        Payment payment = paymentService.pay(request);
        return ResponseEntity.ok(payment.getId());
    }
}
