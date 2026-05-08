package core.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import core.dto.FileDto;
import core.dto.MemberDto;
import core.utill.EncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static core.entity.QMember.member;
import static core.entity.QPartner.partner;
import static core.entity.QMemberPhoto.memberPhoto;

@Repository
public class MemberRepositoryCustom {

    private final JPAQueryFactory query;

    @Autowired
    public MemberRepositoryCustom(JPAQueryFactory query) {
        this.query = query;
    }

    public Page<MemberDto> getMemberList(MemberDto dto, Pageable pageable) throws Exception {
        long offset = pageable.getOffset();

        List<MemberDto> list = query.select(Projections.bean(MemberDto.class,
                        member.ucode,
                        member.memorialCode,
                        member.ptLinkCode,
                        member.funeralUrl,
                        member.funeralDate,
                        member.mobileNo,
                        member.uname,
                        partner.ptName,
                        member.status
                ))
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(
                        eqFuneralDate(dto.startDate, dto.endDate),
                        eqKeyValue(dto.keyType, dto.keyValue),
                        eqPartner(dto.ptLinkCode)
                )
                .offset(offset)
                .limit(pageable.getPageSize())
                .fetch();

        long count = query.select(member.count())
                .from(member)
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(
                        eqFuneralDate(dto.startDate, dto.endDate),
                        eqKeyValue(dto.keyType, dto.keyValue),
                        eqPartner(dto.ptLinkCode)
                )
                .fetchCount();

        return new PageImpl<>(list, pageable, count);
    }

    public Page<MemberDto> getMemberListByPartner(MemberDto dto, Pageable pageable) throws Exception {
        long offset = pageable.getOffset();

        List<MemberDto> list = query.select(Projections.bean(MemberDto.class,
                        member.ucode,
                        member.ptLinkCode,
                        member.memorialCode,
                        member.videoUrl,
                        member.regDate,
                        member.mobileNo,
                        member.uname,
                        member.funeralUrl,
                        partner.ptName,
                        member.status,
                        member.petName
                ))
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(
                        eqRegDate(dto.startDate, dto.endDate),
                        eqUNameAndPetName(dto.keyValue),
                        member.ptLinkCode.eq(dto.ptLinkCode)
                )
                .offset(offset)
                .limit(pageable.getPageSize())
                .fetch();

        long count = query.select(member.count())
                .from(member)
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(
                        eqRegDate(dto.startDate, dto.endDate),
                        eqUNameAndPetName(dto.keyValue),
                        member.ptLinkCode.eq(dto.ptLinkCode)
                )
                .fetchCount();

        return new PageImpl<>(list, pageable, count);
    }


    public List<MemberDto> downloadMemberListByPartnerExcel(MemberDto dto) throws Exception {

        List<MemberDto> list = query.select(Projections.bean(MemberDto.class,
                        member.ucode,
                        member.ptLinkCode,
                        member.createUserUrl,
                        member.regDate,
                        member.mobileNo,
                        member.uname,
                        member.funeralUrl,
                        member.petName,
                        member.status,
                        partner.ptName
                ))
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(
                        eqRegDate(dto.startDate, dto.endDate),
                        eqUNameAndPetName(dto.keyValue),
                        member.ptLinkCode.eq(dto.ptLinkCode)
                )
                .fetch();

        return list;
    }


    public List<MemberDto> getMemberListForExcel(MemberDto dto) throws Exception {

        List<MemberDto> list = query.select(Projections.bean(MemberDto.class,
                        member.memorialCode,
                        member.funeralUrl,
                        member.funeralDate,
                        member.regDate,
                        member.mobileNo,
                        member.uname,
                        partner.ptName
                ))
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(
                        eqRegDate(dto.startDate, dto.endDate),
                        eqKeyValue(dto.keyType, dto.keyValue),
                        eqPartner(dto.ptLinkCode)
                )
                .fetch();

        return list;
    }



    public MemberDto getMemberInfo(String memorialCode) {
        MemberDto dto = query.select(Projections.bean(MemberDto.class,
                        member.petName,
                        member.ucode,
                        member.videoUrl,
                        member.funeralDate,
                        member.expDate,
                        member.limitDate,
                        member.memberSttus,
                        memberPhoto.fileDir.as("m01"),
                        member.skinId,
                        partner.homepage,
                        partner.ptName,
                        partner.ptLogoImg))
                .from(member)
                .join(memberPhoto)
                .on(memberPhoto.memorialCode.eq(member.memorialCode).and(memberPhoto.type.eq("render")).and(memberPhoto.fileId.eq("m01")))
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(member.memorialCode.eq(memorialCode))
                .fetchOne();
        return dto;
    }

    public MemberDto getMemberDtl(String ucode) {
        MemberDto dto = query.select(Projections.bean(MemberDto.class,
                        member.petName,
                        member.uname,
                        member.mobileNo,
                        member.ptLinkCode,
                        member.funeralDate,
                        member.funeralUrl,
                        member.memo,
                        partner.ptName))
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(member.ptLinkCode))
                .where(member.ucode.eq(ucode))
                .fetchOne();
        return dto;
    }



    public MemberDto getPartnerAdminMemberDtl(String memorialCode) {
        MemberDto dto = query.select(Projections.bean(MemberDto.class,
                        member.petName,
                        member.uname,
                        member.mobileNo,
                        member.ptLinkCode,
                        member.funeralDate,
                        member.funeralUrl,
                        member.createUserUrl,
                        member.memorialCode,
                        member.regDate,
                        member.videoUrl,
                        member.memo))
                .from(member)
                .where(member.memorialCode.eq(memorialCode))
                .fetchOne();
        return dto;
    }


    public void uploadForm(FileDto dto) {
        query.update(member)
                .set(member.funeralDate, dto.getFuneralDate())
                .set(member.updDate, dto.getUpdDate())
                .set(member.termsAgreeDate, dto.getUpdDate())
                .set(member.petName, dto.getPetName())
                .set(member.status, "00")
                .set(member.skinId, dto.getSkinId())
                .where(member.ptLinkCode.eq(dto.getPtLinkCode()),
                        member.memorialCode.eq(dto.getMemorialCode()))
                .execute();
    }


    public MemberDto getMemberSttus(String ptLinkCode, String memorialCode) {

        MemberDto dto = query.select(Projections.bean(MemberDto.class,
                        member.status,
                        member.funeralDate,
                        member.mobileNo,
                        member.petName,
                        member.memberSttus,
                        partner.ptLogoImg))
                .from(member)
                .join(partner)
                .on(partner.ptLinkCode.eq(ptLinkCode))
                .where(member.ptLinkCode.eq(ptLinkCode),
                        member.memorialCode.eq(memorialCode))
                .fetchOne();

        return dto;

    }

    public List<FileDto> getThumbImg(String memorialCode) {
        List<FileDto> list = query.select(Projections.bean(FileDto.class,
                        memberPhoto.fileDir,
                        memberPhoto.fileId,
                        memberPhoto.type))
                .from(memberPhoto)
                .where(memberPhoto.memorialCode.eq(memorialCode),
                        memberPhoto.type.eq("render"))
                .fetch();
        return list;
    }

    public MemberDto getSaveMemberInfo(String memorialCode) {
        return query.select(Projections.bean(MemberDto.class,
                        member.mobileNo,
                        member.funeralDate,
                        member.petName,
                        member.skinId,
                        member.terms1,
                        member.terms2
                ))
                .from(member)
                .where(member.memorialCode.eq(memorialCode))
                .fetchOne();
    }

    public List<FileDto> getUploadPhoto(String memorialCode) {
        return query.select(Projections.bean(FileDto.class,
                        memberPhoto.fileDir,
                        memberPhoto.fileId))
                .from(memberPhoto)
                .where(memberPhoto.memorialCode.eq(memorialCode))
                .fetch();
    }


    public void savePetName(String petName, String memorialCode) {
        query.update(member)
                .set(member.petName, petName)
                .where(member.memorialCode.eq(memorialCode))
                .execute();
    }

    public void saveSkinId(String skinId, String memorialCode) {
        query.update(member)
                .set(member.skinId, skinId)
                .where(member.memorialCode.eq(memorialCode))
                .execute();
    }

    public void saveMobileNo(String mobileNo, String memorialCode) {
        query.update(member)
                .set(member.mobileNo, mobileNo)
                .where(member.memorialCode.eq(memorialCode))
                .execute();
    }

    public void saveTerms1(String terms1, String memorialCode) {
        query.update(member)
                .set(member.terms1, terms1)
                .where(member.memorialCode.eq(memorialCode))
                .execute();
    }

    public void saveTerms2(String terms2, String memorialCode) {
        query.update(member)
                .set(member.terms2, terms2)
                .where(member.memorialCode.eq(memorialCode))
                .execute();
    }

    public void saveFuneralDate(LocalDateTime funeralDate, String memorialCode) {
        query.update(member)
                .set(member.funeralDate, funeralDate)
                .where(member.memorialCode.eq(memorialCode))
                .execute();
    }


    public String deleteMember(String ucode) {
        query.delete(member)
                .where(member.ucode.eq(ucode))
                .execute();
        return "00";
    }

    public void deleteMemberPhoto(String memorialCode) {
        query.delete(memberPhoto)
                .where(memberPhoto.memorialCode.eq(memorialCode))
                .execute();
    }


    public String editMemberDtl(String ucode, String uname, String mobileNo, LocalDateTime funeralDate, String memo) {
        query.update(member)
                .set(member.uname, uname)
                .set(member.mobileNo, mobileNo)
                .set(member.funeralDate, funeralDate)
                .set(member.memo, memo)
                .where(member.ucode.eq(ucode))
                .execute();

        return "00";
    }

    public String stopMember(String ucode) {
        query.update(member)
                .set(member.memberSttus, "90")
                .where(member.ucode.eq(ucode))
                .execute();

        return "00";
    }

    public String restoreMember(String ucode) {
        query.update(member)
                .set(member.memberSttus, "00")
                .where(member.ucode.eq(ucode))
                .execute();

        return "00";
    }


    public List<FileDto> findBackUpImg() {
        List<FileDto> list = query.select(Projections.bean(FileDto.class,
                        memberPhoto.memorialCode,
                        memberPhoto.fileId,
                        memberPhoto.fileDir,
                        memberPhoto.backUpSttus))
                .from(memberPhoto)
                .where(memberPhoto.backUpSttus.eq("90"))
                .fetch();
        return list;
    }


    public List<FileDto> getDownloadImg(String memorialCode) {
        List<FileDto> list = query.select(Projections.bean(FileDto.class,
                        memberPhoto.fileId,
                        memberPhoto.fileDir))
                .from(memberPhoto)
                .where(memberPhoto.memorialCode.eq(memorialCode))
                .fetch();
        return list;
    }


    public void updateBackUpSttus(String memorialCode, String fileId) {
        query.update(memberPhoto)
                .set(memberPhoto.backUpSttus, "00")
                .where(memberPhoto.memorialCode.eq(memorialCode),
                        memberPhoto.fileId.eq(fileId))
                .execute();
    }



    public MemberDto getFuneralDate(String ucode) {
        MemberDto dto = query.select(Projections.bean(MemberDto.class,
                        member.funeralDate,
                        member.regDate))
                .from(member)
                .where(member.ucode.eq(ucode))
                .fetchOne();
        return dto;
    }

    public LocalDateTime getRegDate(String ucode) {
        LocalDateTime regDate = query.select(member.regDate)
                .from(member)
                .where(member.ucode.eq(ucode))
                .fetchOne();
        return regDate;
    }

    BooleanExpression eqPartner(String ptLinkCode) {
        BooleanExpression result = null;
        if (StringUtils.hasText(ptLinkCode)) {
            if (!ptLinkCode.equals("")) {
                result = partner.ptLinkCode.eq(ptLinkCode);
            }
        }
        return result;
    }


    BooleanExpression eqKeyValue(String keyType, String keyValue) throws Exception {
        BooleanExpression result = null;
        if (StringUtils.hasText(keyType) && StringUtils.hasText(keyValue)) {
            switch (keyType) {
                case "":
                    result = null;
                    break;
                case "name":
                    result = member.uname.contains(keyValue);
                    break;
                case "mobileNo":
                    String mobileNo = EncryptionUtil.encryption(keyValue);
                    result = member.mobileNo.eq(mobileNo);
                    break;
            }
        }
        return result;
    }

    BooleanExpression eqUNameAndPetName(String keyValue) throws Exception {
        BooleanExpression result = null;
        if (StringUtils.hasText(keyValue)) {
            result = member.uname.contains(keyValue).or(member.petName.contains(keyValue));
        }
        return result;
    }


    BooleanExpression eqFuneralDate(String startDate, String endDate) {
        BooleanExpression result = null;
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            startDate = startDate + "T00:00:00";
            endDate = endDate + "T23:59:59";
            result = member.funeralDate.between(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
        }
        return result;
    }

    BooleanExpression eqRegDate(String startDate, String endDate) {
        BooleanExpression result = null;
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            startDate = startDate + "T00:00:00";
            endDate = endDate + "T23:59:59";
            result = member.regDate.between(LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
        }
        return result;
    }

}
