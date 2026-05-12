package core.domain.member.repository;

import core.domain.member.entity.PaymentMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMemberRepository extends JpaRepository<PaymentMember, Long> {
    Optional<PaymentMember> findByEmail(String email);
}
