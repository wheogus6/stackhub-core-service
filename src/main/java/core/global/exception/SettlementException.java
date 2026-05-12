package core.global.exception;

/**
 * 정산 도메인 예외
 * CustomException 상속 → GlobalExceptionHandler에서 자동 처리
 */
public class SettlementException extends CustomException {

    public SettlementException(ResponseCode code) {
        super(code);
    }

    // 이미 정산된 내역 존재
    public static class AlreadyExistsException extends SettlementException {
        public AlreadyExistsException() { super(ResponseCode.SETTLEMENT_ALREADY_EXISTS); }
    }

    // 정산 배치 실패
    public static class BatchFailException extends SettlementException {
        public BatchFailException() { super(ResponseCode.SETTLEMENT_BATCH_FAIL); }
    }
}
