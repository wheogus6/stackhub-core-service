package core.repository;



import core.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findBySettledDateAndStatus(LocalDate settledDate, Settlement.SettlementStatus status);
}
