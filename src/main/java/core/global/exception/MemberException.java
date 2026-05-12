package core.global.exception;

/**
 * 회원 도메인 예외
 * CustomException 상속 → GlobalExceptionHandler에서 자동 처리
 */
public class MemberException extends CustomException {

    public MemberException(ResponseCode code) {
        super(code);
    }

    // 회원 없음
    public static class NotFoundException extends MemberException {
        public NotFoundException() { super(ResponseCode.MEMBER_NOT_FOUND); }
    }

    // 잔액 부족
    public static class BalanceNotEnoughException extends MemberException {
        public BalanceNotEnoughException() { super(ResponseCode.MEMBER_BALANCE_NOT_ENOUGH); }
    }

    // 유효하지 않은 금액
    public static class InvalidAmountException extends MemberException {
        public InvalidAmountException() { super(ResponseCode.MEMBER_INVALID_AMOUNT); }
    }
}
