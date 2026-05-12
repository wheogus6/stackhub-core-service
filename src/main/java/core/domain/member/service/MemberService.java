package core.domain.member.service;

import core.domain.member.dto.MemberRegisterDto;
import core.domain.member.entity.PaymentMember;
import core.domain.member.repository.PaymentMemberRepository;
import core.global.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final PaymentMemberRepository memberRepository;

    @Transactional
    public PaymentMember register(MemberRegisterDto request) {
        // 이메일 중복 체크
        memberRepository.findByEmail(request.getEmail())
                .ifPresent(m -> { throw new MemberException.NotFoundException(); });

        PaymentMember member = PaymentMember.create(request.getEmail());
        memberRepository.save(member);

        log.info("[회원 등록] memberId={}, email={}", member.getId(), member.getEmail());
        return member;
    }

    @Transactional(readOnly = true)
    public PaymentMember findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberException.NotFoundException::new);
    }
}
