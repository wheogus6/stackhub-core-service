package core.controller;

import core.dto.ResponseCode;
import core.dto.ResponseDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // 토큰 유효성 확인용 — React에서 진입 시 호출해서 인증 상태 체크 가능
    @GetMapping("/health")
    public ResponseDto health(@AuthenticationPrincipal String userCode) {
        return new ResponseDto(ResponseCode.SUCCESS, userCode);
    }

}
