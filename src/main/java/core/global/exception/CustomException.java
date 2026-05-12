package core.global.exception;

/**
 * 프로젝트 공통 예외 클래스
 *
 * 예외는 반드시 ResponseCode를 통해 생성합니다.
 * throw new CustomException(ResponseCode.MEMBER_NOT_FOUND);
 * throw new CustomException(ResponseCode.INVALID_REQUEST, "idempotencyKey가 필요합니다.");
 */
public class CustomException extends RuntimeException {

    private final ResponseCode responseCode;

    public CustomException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public CustomException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.responseCode = responseCode;
    }

    // 상세 메시지를 별도 지정할 때 사용
    public CustomException(ResponseCode responseCode, String detail) {
        super(detail);
        this.responseCode = responseCode;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }
}
