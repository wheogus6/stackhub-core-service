package core.dto;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS("00", "성공"),
    UNAUTHORIZED("A001", "인증이 필요합니다."),
    FORBIDDEN("A002", "접근 권한이 없습니다.");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
