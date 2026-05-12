package core.domain.payment.service;

import core.domain.member.entity.PaymentMember;
import core.domain.member.repository.PaymentMemberRepository;
import core.domain.payment.dto.ChargeRequestDto;
import core.global.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeService {

    private final PaymentMemberRepository memberRepository;

    @Transactional
    public Long charge(ChargeRequestDto request) {
        if (request.getAmount() <= 0) {
            throw new MemberException.InvalidAmountException();
        }

        PaymentMember member = memberRepository.findById(request.getMemberId())
                .orElseThrow(MemberException.NotFoundException::new);

        member.charge(request.getAmount());

        log.info("[충전] memberId={}, amount={}, balance={}",
                member.getId(), request.getAmount(), member.getBalance());

        return member.getBalance(); // 충전 후 잔액 반환
    }
}
