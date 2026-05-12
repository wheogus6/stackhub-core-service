package core.domain.member.controller;

import core.domain.member.dto.MemberRegisterDto;
import core.domain.member.entity.PaymentMember;
import core.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "회원 등록",
            description = "이메일로 회원을 등록합니다. 초기 잔액은 0원입니다."
    )
    @PostMapping
    public ResponseEntity<?> register(@RequestBody MemberRegisterDto request) {
        PaymentMember member = memberService.register(request);
        return ResponseEntity.ok(member.getId());
    }

    @Operation(
            summary = "회원 잔액 조회",
            description = "회원의 현재 잔액을 조회합니다."
    )
    @GetMapping("/{memberId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long memberId) {
        PaymentMember member = memberService.findById(memberId);
        return ResponseEntity.ok(member.getBalance());
    }
}
