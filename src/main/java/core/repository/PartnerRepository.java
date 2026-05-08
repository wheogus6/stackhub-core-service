package core.repository;

import core.dto.PartnerDto;
import core.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {

    @Query(value = "SELECT " +
            "p.pt_code as ptCode, " +
            "p.pt_link_code as ptLinkCode, " +
            "p.id, " +
            "p.pt_name as ptName, " +
            "p.tel_no as telNo " +
            "FROM partner p " +
            "WHERE (" +
            "p.sttus != '90' " +
            "and " +
            "(case when :#{#dto.ptName} = '' then p.pt_code is not null " +
            "else p.pt_name like %:#{#dto.ptName}% end)" +
            "and " +
            "(case when :#{#dto.ptLinkCode} = '' then p.pt_code is not null " +
            "else p.pt_link_code = :#{#dto.ptLinkCode} end)) " +
            "ORDER BY p.pt_name ASC " +
            "LIMIT :limit OFFSET :page",
            countQuery = "SELECT COUNT(*) " +
                    "FROM partner p " +
                    "where ( " +
                    "p.sttus != '90' " +
                    "and " +
                    "(case when :#{#dto.ptName} = '' then p.pt_code is not null " +
                    "else p.pt_name like %:#{#dto.ptName}% end)" +
                    "and " +
                    "(case when :#{#dto.ptLinkCode} = '' then p.pt_code is not null " +
                    "else p.pt_link_code = :#{#dto.ptLinkCode} end)) "
            ,nativeQuery = true)
    List<Map<String, Object>> findPartners(@Param("dto") PartnerDto dto, @Param("page") int page, @Param("limit") int limit);


    @Query(value = "SELECT COUNT(*) " +
            "FROM partner p " +
            "where ( " +
            "p.sttus != '90' " +
            "and " +
            "(case when :#{#dto.ptName} = '' then p.pt_code is not null " +
            "else p.pt_name like %:#{#dto.ptName}% end)" +
            "and " +
            "(case when :#{#dto.ptLinkCode} = '' then p.pt_code is not null " +
            "else p.pt_link_code = :#{#dto.ptLinkCode} end)) "
            ,nativeQuery = true)
    long findPartnersAdminCount(@Param("dto") PartnerDto dto);

}

