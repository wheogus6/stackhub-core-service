package core.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import core.dto.PartnerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static core.entity.QPartner.partner;

@Repository
public class PartnerRepositoryCustom {

    private final JPAQueryFactory query;

    @Autowired
    public PartnerRepositoryCustom(JPAQueryFactory query) {
        this.query = query;
    }

    public Page<PartnerDto> getPartnerList(PartnerDto dto, Pageable pageable) {

        List<PartnerDto> list = query.select(Projections.bean(PartnerDto.class,
                        partner.ptCode,
                        partner.ptLinkCode,
                        partner.id,
                        partner.ptName,
                        partner.telNo,
                        partner.ptLinkCode))
                .from(partner)
                .where(
                        eqPartnerName(dto.getPtName()),
                        eqPtLinkCode(dto.getPtLinkCode())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(partner.ptName.asc())
                .fetch();

        long count = query.select(partner.count())
                .from(partner)
                .where(
                        eqPartnerName(dto.getPtName()),
                        eqPtLinkCode(dto.getPtLinkCode())
                )
                .fetchCount();

        return new PageImpl<>(list, pageable, count);
    }

    public PartnerDto getPartnerDtl(String ptlCode) {
        PartnerDto dto = query.select(Projections.bean(PartnerDto.class,
                        partner.id,
                        partner.ptName,
                        partner.telNo,
                        partner.homepage,
                        partner.addr,
                        partner.memo,
                        partner.pwd,
                        partner.ptLogoImg))
                .from(partner)
                .where(partner.ptLinkCode.eq(ptlCode))
                .fetchOne();

        return dto;
    }

    public String editPartner(PartnerDto dto) {
        query.update(partner)
                .set(partner.id, dto.id)
                .set(partner.pwd, dto.pwd)
                .set(partner.telNo, dto.getTelNo())
                .set(partner.ptName, dto.getPtName())
                .set(partner.addr, dto.getAddr())
                .set(partner.homepage, dto.getHomepage())
                .set(partner.memo, dto.getMemo())
                .set(partner.updDate, dto.getUpdDate())
                .set(partner.ptLogoImg, dto.getPtLogoImg())
                .where(partner.ptLinkCode.eq(dto.getPtLinkCode()))
                .execute();
        return "00";
    }


    public long existsByPId(String id) {
        long count = query.select(partner.count())
                .from(partner)
                .where(partner.id.eq(id))
                .fetchCount();
        return count;
    }

    public long existsByPtName(String ptName) {
        long count = query.select(partner.count())
                .from(partner)
                .where(partner.ptName.eq(ptName))
                .fetchCount();
        return count;
    }

    public List<PartnerDto> getAllPartnerList() {
        List<PartnerDto> list = query.select(Projections.bean(PartnerDto.class,
                        partner.ptName,
                        partner.ptLinkCode))
                .from(partner)
                .where(partner.sttus.ne("90"))
                .orderBy(partner.ptName.asc())
                .fetch();

        return list;
    }


    public String deletePartner(String ptCode) {
        query.update(partner)
                .set(partner.sttus, "90")
                .where(partner.ptCode.eq(ptCode))
                .execute();
        return "00";
    }


    public PartnerDto loginCheck(String id) {
        PartnerDto dto = query.select(Projections.bean(PartnerDto.class,
                        partner.ptCode,
                        partner.ptLinkCode,
                        partner.id,
                        partner.pwd,
                        partner.ptName,
                        partner.homepage,
                        partner.sttus))
                .from(partner)
                .where(partner.id.eq(id))
                .fetchOne();

        return dto;
    }


    BooleanExpression eqPartnerName(String ptName) {
        BooleanExpression result = null;
        if (StringUtils.hasText(ptName)) {
            result = partner.ptName.contains(ptName);
        }
        return result;
    }

    BooleanExpression eqPtLinkCode(String ptLinkCode) {
        BooleanExpression result = null;
        if (StringUtils.hasText(ptLinkCode)) {
            result = partner.ptLinkCode.eq(ptLinkCode);
        }
        return result;
    }

}
