package core.global.exception;

/**
 * 결제 도메인 예외
 * CustomException 상속 → GlobalExceptionHandler에서 자동 처리
 *
 * 사용법:
 * throw new PaymentException.BalanceNotEnoughException();
 * throw new PaymentException.DuplicateRequestException();
 */
public class PaymentException extends CustomException {

    public PaymentException(ResponseCode code) {
        super(code);
    }

    // 잔액 부족
    public static class BalanceNotEnoughException extends PaymentException {
        public BalanceNotEnoughException() { super(ResponseCode.PAYMENT_BALANCE_NOT_ENOUGH); }
    }

    // 중복 결제 요청
    public static class DuplicateRequestException extends PaymentException {
        public DuplicateRequestException() { super(ResponseCode.PAYMENT_DUPLICATE_REQUEST); }
    }

    // 분산락 획득 실패
    public static class LockFailedException extends PaymentException {
        public LockFailedException() { super(ResponseCode.PAYMENT_LOCK_FAILED); }
    }

    // 이미 완료된 결제
    public static class AlreadyCompletedException extends PaymentException {
        public AlreadyCompletedException() { super(ResponseCode.PAYMENT_ALREADY_COMPLETED); }
    }

    // 이미 취소된 결제
    public static class AlreadyCancelledException extends PaymentException {
        public AlreadyCancelledException() { super(ResponseCode.PAYMENT_ALREADY_CANCELLED); }
    }
}
