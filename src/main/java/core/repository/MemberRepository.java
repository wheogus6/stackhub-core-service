package core.repository;

import core.dto.MemberDto;
import core.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {


    @Query(value = "SELECT " +
            "m.ucode as ucode, " +
            "m.memorial_code as memorialCode, " +
            "m.pt_link_code as ptLinkCode, " +
            "m.funeral_url as funeralUrl, " +
            "m.mobile_no as mobileNo, " +
            "m.uname as uname, " +
            "m.status as status, " +
            "m.member_sttus as memberSttus, " +
            "p.pt_name as ptName " +
            "FROM member m " +
            "JOIN partner p " +
            "on p.pt_link_code = m.pt_link_code " +
            "WHERE (" +
            "(case when :#{#dto.startDate} != '' then m.reg_date between to_timestamp(:#{#dto.startDate},'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:#{#dto.endDate} || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS') else  m.reg_date is not null end) " +
            "and " +
            "(case when :#{#dto.keyType} = '' then m.ucode is not null " +
            "when :#{#dto.keyType} = 'name' then m.uname like %:#{#dto.keyValue}% " +
            "when :#{#dto.keyType} = 'mobileNo' then m.mobile_no = :#{#dto.keyValue} end) "+
            "and " +
            "(case when :#{#dto.ptLinkCode} = '' then m.ucode is not null " +
            "else p.pt_link_code = :#{#dto.ptLinkCode} end) " +
            ")" +
            "ORDER BY m.reg_date DESC " +
            "LIMIT :limit OFFSET :page",
            countQuery = "SELECT COUNT(*) " +
                    "FROM member m " +
                    "JOIN partner p " +
                    "on p.pt_link_code = m.pt_link_code " +
                    "WHERE (" +
                    "(case when :#{#dto.startDate} != '' then m.reg_date between to_timestamp(:#{#dto.startDate},'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:#{#dto.endDate} || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS') else  m.reg_date is not null end) " +
                    "and " +
                    "(case when :#{#dto.keyType} = '' then m.ucode is not null " +
                    "when :#{#dto.keyType} = 'name' then m.uname like %:#{#dto.keyValue}% " +
                    "when :#{#dto.keyType} = 'mobileNo' then m.mobile_no = :#{#dto.keyValue} end) "+
                    "and " +
                    "(case when :#{#dto.ptLinkCode} = '' then p.pt_code is not null " +
                    "else p.pt_link_code = :#{#dto.ptLinkCode} end)) "
            ,nativeQuery = true)
    List<Map<String, Object>> findMembers(@Param("dto") MemberDto dto, @Param("page") int page, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) " +
            "FROM member m " +
            "JOIN partner p " +
            "on p.pt_link_code = m.pt_link_code " +
            "WHERE (" +
            "(case when :#{#dto.startDate} != '' then m.reg_date between to_timestamp(:#{#dto.startDate},'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:#{#dto.endDate} || ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS') else  m.reg_date is not null end) " +
            "and " +
            "(case when :#{#dto.keyType} = '' then m.ucode is not null " +
            "when :#{#dto.keyType} = 'name' then m.uname like %:#{#dto.keyValue}% " +
            "when :#{#dto.keyType} = 'mobileNo' then m.mobile_no = :#{#dto.keyValue} end) "+
            "and " +
            "(case when :#{#dto.ptLinkCode} = '' then p.pt_code is not null " +
            "else p.pt_link_code = :#{#dto.ptLinkCode} end)) "
            ,nativeQuery = true)
    long findMembersCount(@Param("dto") MemberDto dto);






    @Query(value = "SELECT " +
            "m.ucode as ucode, " +
            "m.memorial_code as memorialCode, " +
            "m.pt_link_code as ptLinkCode, " +
            "m.funeral_url as funeralUrl, " +
            "m.mobile_no as mobileNo, " +
            "m.uname as uname, " +
            "m.status as status, " +
            "m.pet_name as petName, " +
            "p.pt_name as ptName, " +
            "m.video_url as videoUrl " +
            "FROM member m " +
            "JOIN partner p " +
            "on p.pt_link_code = m.pt_link_code " +
            "WHERE (" +
            "(case when :#{#dto.startDate} != '' then m.reg_date between to_timestamp(:#{#dto.startDate},'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:#{#dto.endDate}|| ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS') else  m.reg_date is not null end) " +
            "and " +
            "(case when :#{#dto.keyValue} = '' then m.ucode is not null " +
            "else m.uname like %:#{#dto.keyValue}% or m.pet_name like %:#{#dto.keyValue}% end) "+
            "and " +
            "m.pt_link_code = :#{#dto.ptLinkCode} " +
            ")" +
            "ORDER BY m.reg_date DESC " +
            "LIMIT :limit OFFSET :page",
            countQuery = "SELECT COUNT(*) " +
                    "FROM member m " +
                    "JOIN partner p " +
                    "on p.pt_link_code = m.pt_link_code " +
                    "WHERE (" +
                    "(case when :#{#dto.startDate} != '' then m.reg_date between to_timestamp(:#{#dto.startDate},'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:#{#dto.endDate}|| ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS') else  m.reg_date is not null end) " +
                    "and " +
                    "(case when :#{#dto.keyValue} = '' then m.ucode is not null " +
                    "else m.uname like %:#{#dto.keyValue}% or m.pet_name like %:#{#dto.keyValue}% end) "+
                    "and " +
                    "m.pt_link_code = :#{#dto.ptLinkCode} " +
                    ")"
            ,nativeQuery = true)
    List<Map<String, Object>> findMembersByPartnerAdmin(@Param("dto") MemberDto dto, @Param("page") int page, @Param("limit") int limit);


    @Query(value = "SELECT COUNT(*) " +
            "FROM member m " +
            "JOIN partner p " +
            "on p.pt_link_code = m.pt_link_code " +
            "WHERE (" +
            "(case when :#{#dto.startDate} != '' then m.reg_date between to_timestamp(:#{#dto.startDate},'YYYY-MM-DD HH24:MI:SS') and to_timestamp(:#{#dto.endDate}|| ' 23:59:59', 'YYYY-MM-DD HH24:MI:SS') else  m.reg_date is not null end) " +
            "and " +
            "(case when :#{#dto.keyValue} = '' then m.ucode is not null " +
            "else m.uname like %:#{#dto.keyValue}% or m.pet_name like %:#{#dto.keyValue}% end) "+
            "and " +
            "m.pt_link_code = :#{#dto.ptLinkCode} " +
            ")"
            ,nativeQuery = true)
    long findMembersByPartnerAdminCount(@Param("dto") MemberDto dto);


}
