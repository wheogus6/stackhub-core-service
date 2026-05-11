package core.controller;


import core.dto.PaymentRequestDto;
import core.entity.Payment;
import core.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody PaymentRequestDto request) {
        Payment payment = paymentService.pay(request);
        return ResponseEntity.ok(payment.getId());
    }
}
