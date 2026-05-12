package core.global.exception;

import core.global.exception.CustomException;
import core.global.exception.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     * PaymentException, SettlementException, MemberException 모두 여기서 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        ResponseCode code = ex.getResponseCode();
        log.warn("[CustomException] code={}, message={}", code.getCode(), ex.getMessage());
        return ResponseEntity
                .status(code.getStatus())
                .body(Map.of(
                        "code", code.getCode(),
                        "message", ex.getMessage()
                ));
    }

    /**
     * 미처리 예외 — 최후 방어선
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnknown(Exception ex) {
        log.error("[UnhandledException] {}", ex.getMessage(), ex);
        ResponseCode code = ResponseCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(code.getStatus())
                .body(Map.of(
                        "code", code.getCode(),
                        "message", code.getMessage()
                ));
    }
}
