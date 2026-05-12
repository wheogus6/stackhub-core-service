package core.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 전체 응답 코드 레지스트리
 *
 * 코드 규칙:
 *   00       성공
 *   1xxx     공통 오류
 *   5xxx     서버 오류
 *   D###     도메인 Not Found
 *   PM###    결제 도메인
 *   ST###    정산 도메인
 *   MB###    회원 도메인
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    // ── 성공 ────────────────────────────────────────────────────────────────
    SUCCESS("00", "성공", HttpStatus.OK),

    // ── 공통 오류 ────────────────────────────────────────────────────────────
    NOT_FOUND("1001", "데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("1002", "유효하지 않은 요청입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_ERROR("1003", "중복된 데이터가 존재합니다.", HttpStatus.BAD_REQUEST),

    // ── 서버 오류 ────────────────────────────────────────────────────────────
    INTERNAL_SERVER_ERROR("5000", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // ── 도메인 Not Found (D###) ──────────────────────────────────────────────
    MEMBER_NOT_FOUND("D001", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND("D002", "결제 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SETTLEMENT_NOT_FOUND("D003", "정산 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // ── 결제 도메인 (PM###) ──────────────────────────────────────────────────
    PAYMENT_BALANCE_NOT_ENOUGH("PM001", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_DUPLICATE_REQUEST("PM002", "중복 결제 요청입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_LOCK_FAILED("PM003", "결제 처리 중입니다. 잠시 후 다시 시도해주세요.", HttpStatus.CONFLICT),
    PAYMENT_ALREADY_COMPLETED("PM004", "이미 완료된 결제입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_CANCELLED("PM005", "이미 취소된 결제입니다.", HttpStatus.BAD_REQUEST),

    // ── 정산 도메인 (ST###) ──────────────────────────────────────────────────
    SETTLEMENT_ALREADY_EXISTS("ST001", "이미 정산된 내역이 존재합니다.", HttpStatus.BAD_REQUEST),
    SETTLEMENT_BATCH_FAIL("ST002", "정산 배치 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // ── 회원 도메인 (MB###) ──────────────────────────────────────────────────
    MEMBER_BALANCE_NOT_ENOUGH("MB001", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    MEMBER_INVALID_AMOUNT("MB002", "유효하지 않은 금액입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
