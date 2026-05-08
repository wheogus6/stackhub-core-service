package core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.dto.*;
import core.entity.Comment;
import core.entity.Member;
import core.repository.*;
import core.utill.CommonUtill;
import core.utill.EncryptionUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.management.openmbean.InvalidKeyException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FileService fileService;
    @Autowired
    MemberRepositoryCustom memberRepositoryCustom;
    @Autowired
    PartnerRepositoryCustom partnerRepositoryCustom;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentRepositoryCustom commentRepositoryCustom;
    @Autowired
    CommonUtill commonUtill;
    @Autowired
    RestTemplate restTemplate;



    @Transactional
    public MemberDto createMember(String name, String mobileNo, String ptLinkCode, String homepage) throws Exception {
        Member member = new Member();
        LocalDateTime now = LocalDateTime.now();

        String uCode = commonUtill.createNewCode("member");
        String memorialCode = commonUtill.createNewCode("memorial");
        String saveMobileNo = EncryptionUtil.encryption(mobileNo);
        // 우선 로컬로 저장
        String domain = "https://petcine.com";

        String createUserUrl = domain + "/gday/front/create?memberCreateCode=" + ptLinkCode + "-" + memorialCode;

        member.setUcode(uCode);
        member.setMobileNo(saveMobileNo);
        member.setRegDate(now);
        member.setUname(name);
        member.setPtLinkCode(ptLinkCode);
        member.setMemorialCode(memorialCode);
        member.setCreateUserUrl(createUserUrl);
        member.setMemberSttus("00");
        //TODO // 상태 00 : 업로드 완료 , 10 : 렌더 완료, 90 : 미업로드
        member.setStatus("90");
        memberRepository.save(member);

        MemberDto dto = new MemberDto();
        dto.setCreateUserUrl(createUserUrl);
        dto.setUname(name);
        dto.setMobileNo(mobileNo);


//        MemberPhoto memberPhoto

//        PartnerDto partnerDto = partnerRepositoryCustom.getPartnerDtl(ptLinkCode);

        /** 네이버 sms 발송 */
//        sendAlertSms(partnerDto, createUserUrl, mobileNo, name);

        return dto;
    }


    private void sendAlertSms(PartnerDto partnerDto, String createUserUrl, String mobileNo, String name) {
        try {
            String serviceId = "ncp:sms:kr:283583576582:gday";
            String aKey = "HBXcD9sC2aOdNGuyp1qg";
            String sKey = "Cozu9tuSHN9P7qCjMlwCFp3D4QCEoKxfhKTYxIcd";
            String url = "https://sens.apigw.ntruss.com/sms/v2/services/" + serviceId + "/messages";

            long time = System.currentTimeMillis();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-ncp-apigw-timestamp", String.valueOf(time));
            headers.set("x-ncp-iam-access-key", aKey);
            headers.set("x-ncp-apigw-signature-v2", makeSignature(serviceId, aKey, sKey, time));

            String smsContents = "안녕하세요, "+ name +"님.\n\n" + "소중한 반려동물의 추모 영상을 제작하기 위한 안내 드립니다.\n\n" + "영상 제작을 위해 아래 링크를 클릭하여 정보를 입력해주세요.\n\n" + createUserUrl + "\n\n기타 문의 사항이 있는 경우 아래 연락처로 문의 주세요.\n\n" + partnerDto.getPtName() + "\n" + EncryptionUtil.decryption(partnerDto.getTelNo()) + "\n\n감사합니다.";

            MessageDto messageDto = new MessageDto();
            messageDto.setTo(mobileNo);
            messageDto.setContent(smsContents);
            List<MessageDto> memberDtoList = new ArrayList<>();
            memberDtoList.add(messageDto);
            log.info("sms sent headers " + headers);
            SmsDto request = SmsDto.builder()
                    .type("LMS")
                    .contentType("COMM")
                    .countryCode("82")
                    .from("0327103599")
                    .content(messageDto.getContent())
                    .messages(memberDtoList)
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            String body = objectMapper.writeValueAsString(request);
            HttpEntity<String> httpBody = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, httpBody, String.class);

            log.info("sms sent successfully " + mobileNo);
        } catch (Exception e) {
            log.info("sms submit error: " + e.getMessage() + "couse =======================>" + e.getCause());
            e.printStackTrace();
        }
    }


    public Page<MemberDto> getMemberList(MemberDto dto, Pageable pageable) throws Exception {

        int page = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        if (dto.getKeyType().equals("mobileNo")) {
            if (!dto.getKeyValue().equals("")) {
                dto.setKeyValue(EncryptionUtil.encryption(dto.getKeyValue()));
            }
        }

        List<Map<String, Object>> list2 = memberRepository.findMembers(dto, page, limit);

        List<MemberDto> MemberDtos = list2.stream().map(map -> {
            String mobileNo = null;
            MemberDto dto1 = memberRepositoryCustom.getFuneralDate((String) map.get("ucode"));
            String funeralStr = "미업로드";
            String regDateStr = "";
            String funeralUrl = "미업로드";
            if (map.get("funeralUrl") != null) {
                funeralUrl = map.get("funeralUrl").toString();
            }

            try {
                mobileNo = EncryptionUtil.decryption(map.get("mobileNo").toString());
                funeralStr = dto1.getFuneralDate() != null ? commonUtill.dateFormatter(dto1.getFuneralDate()) : "미업로드";
                regDateStr = dto1.getRegDate() != null ? commonUtill.dateAndTimeFormatter(dto1.getRegDate()) : "-";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            MemberDto memberDto = new MemberDto();
            memberDto.setUcode(map.get("ucode").toString());
            memberDto.setMemorialCode(map.get("memorialCode").toString());
            memberDto.setPtLinkCode(map.get("ptLinkCode").toString());
            memberDto.setFuneralUrl(funeralUrl);
            memberDto.setFuneralDateStr(funeralStr);
            memberDto.setMobileNo(commonUtill.addDashForTelNo(mobileNo));
            memberDto.setUname(map.get("uname").toString());
            memberDto.setStatus(map.get("status").toString());
            memberDto.setPtName(map.get("ptName").toString());
            memberDto.setRegDateStr(regDateStr);
            memberDto.setMemberSttus(map.get("memberSttus").toString());
            return memberDto;
        }).collect(Collectors.toList());

        long count = memberRepository.findMembersCount(dto);

        return new PageImpl<>(MemberDtos, pageable, count);

    }


    public Page<MemberDto> getMemberListByPartner(MemberDto dto, Pageable pageable) throws Exception {

        int page = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        List<Map<String, Object>> list2 = memberRepository.findMembersByPartnerAdmin(dto, page, limit);

        List<MemberDto> MemberDtos = list2.stream().map(map -> {
            String mobileNo = null;
            LocalDateTime regDate = memberRepositoryCustom.getRegDate((String) map.get("ucode"));
            String regDateStr = "미업로드";
            String petName = null;
            String videoUrl = null;
            try {
                mobileNo = EncryptionUtil.decryption(map.get("mobileNo").toString());
                regDateStr = regDate != null ? commonUtill.dateFormatter(regDate) : "미업로드";
                petName = map.get("petName") != null ? map.get("petName").toString() : null;
                videoUrl = map.get("videoUrl") != null ? map.get("videoUrl").toString() : null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            MemberDto memberDto = new MemberDto();
            memberDto.setUcode(map.get("ucode").toString());
            memberDto.setMemorialCode(map.get("memorialCode").toString());
            memberDto.setPtLinkCode(map.get("ptLinkCode").toString());
//            memberDto.setFuneralUrl(map.get("funeralUrl").toString());
            memberDto.setRegDateStr(regDateStr);
            memberDto.setMobileNo(commonUtill.addDashForTelNo(mobileNo));
            memberDto.setUname(map.get("uname").toString());
            memberDto.setStatus(map.get("status").toString());
            memberDto.setPtName(map.get("ptName").toString());
            memberDto.setPetName(petName);
            memberDto.setVideoUrl(videoUrl);



            return memberDto;
        }).collect(Collectors.toList());


        long count = memberRepository.findMembersByPartnerAdminCount(dto);


        return new PageImpl<>(MemberDtos, pageable, count);

    }

    public List<MemberDto> downloadMemberListByPartnerExcel(MemberDto dto) throws Exception {
        List<MemberDto> list = memberRepositoryCustom.downloadMemberListByPartnerExcel(dto);

        for (MemberDto memberDto : list) {
            String mobileNo = memberDto.getMobileNo() != null ? EncryptionUtil.decryption((memberDto.getMobileNo())) : null;
            if (mobileNo != null) {
                memberDto.setMobileNo(commonUtill.addDashForTelNo(mobileNo));
            }

            String regDate = memberDto.getRegDate() != null ? commonUtill.dateFormatter(memberDto.getRegDate()) : null;
            if (regDate != null) {
                memberDto.setRegDateStr(regDate);
            }
        }

        return list;
    }


    public List<MemberDto> downloadMemberListExcel(MemberDto dto) throws Exception {
        List<MemberDto> list = memberRepositoryCustom.getMemberListForExcel(dto);

        for (MemberDto memberDto : list) {
            String mobileNo = memberDto.getMobileNo() != null ? EncryptionUtil.decryption((memberDto.getMobileNo())) : null;
            if (mobileNo != null) {
                memberDto.setMobileNo(commonUtill.addDashForTelNo(mobileNo));
            }

            String regDate = memberDto.getRegDate() != null ? commonUtill.dateFormatter(memberDto.getRegDate()) : null;
            if (regDate != null) {
                memberDto.setRegDateStr(regDate);
            }

            String funeralDate = memberDto.getFuneralDate() != null ? commonUtill.dateAndTimeFormatter(memberDto.getFuneralDate()) : "미업로드";
            memberDto.setFuneralDateStr(funeralDate);
        }

        return list;
    }


    public MemberDto getMemberInfo(String memorialCode) {
        MemberDto dto = memberRepositoryCustom.getMemberInfo(memorialCode);

        //오늘 날짜
        LocalDate nowDate = LocalDate.now();

        //무지개 다리 건넌지 얼마나 지났는지
        LocalDate funeral = dto.getFuneralDate().toLocalDate();
        long countDate = ChronoUnit.DAYS.between(funeral, nowDate);
        dto.setCountDate(countDate);

        // 추모관 이용일 구하기
        LocalDate exp = dto.getExpDate().toLocalDate();
        long limit = ChronoUnit.DAYS.between(exp, nowDate);
        Integer limitDate = dto.getLimitDate();
        long finishDate = limitDate - limit;
        dto.setFinishDate(finishDate);

        return dto;
    }

    @Transactional
    public String commentWrite(String memorialCode, String name, String comment) {

        Comment saveComment = new Comment();

        saveComment.setMemorialCode(memorialCode);
        saveComment.setName(StringEscapeUtils.escapeHtml4(name));
        saveComment.setComment(StringEscapeUtils.escapeHtml4(comment));
        LocalDateTime now = LocalDateTime.now();
        saveComment.setRegDate(now);

        commentRepository.save(saveComment);

        return "00";
    }

    public List<CommentDto> getCommentList(String memorialCode) {
        return commentRepositoryCustom.getCommentList(memorialCode);
    }

    @Transactional
    public String editComment(Long id, String comment, String memorialCode) {
        String result = commentRepositoryCustom.editComment(id, comment, memorialCode);
        return result;
    }

    @Transactional
    public String deleteComment(Long id, String memorialCode) {
        String result = commentRepositoryCustom.deleteComment(id, memorialCode);
        return result;
    }


    public MemberDto getMemberDtl(String ucode) throws Exception {
        MemberDto dto = memberRepositoryCustom.getMemberDtl(ucode);
        String fd = dto.getFuneralDate() != null ? commonUtill.dateFormatter(dto.getFuneralDate()) : "미업로드";
        String mobileNo = dto.getMobileNo() != null ? EncryptionUtil.decryption(dto.getMobileNo()) : "-";

        dto.setFuneralDateStr(fd);
        dto.setMobileNo(mobileNo);

        return dto;
    }

    public MemberDto getPartnerAdminMemberDtl(String memorialCode) throws Exception {
        MemberDto dto = memberRepositoryCustom.getPartnerAdminMemberDtl(memorialCode);
        String fd = dto.getRegDate() != null ? commonUtill.dateFormatter(dto.getRegDate()) : "미업로드";
        String mobileNo = dto.getMobileNo() != null ? EncryptionUtil.decryption(dto.getMobileNo()) : "-";

        dto.setRegDateStr(fd);
        dto.setMobileNo(mobileNo);

        return dto;
    }


    @Transactional
    public String editMemberDtl(String ucode, String uname, String mobileNo, String funeralDate, String memo) throws Exception {

        String saveMobileNo = EncryptionUtil.encryption(mobileNo);

        String saveUName = StringEscapeUtils.escapeHtml4(uname);


        String saveMemo = StringEscapeUtils.escapeHtml4(memo);

        String funeral = funeralDate + "T00:00:00";
        LocalDateTime saveFuneralDate = LocalDateTime.parse(funeral);

        String result = memberRepositoryCustom.editMemberDtl(ucode, saveUName, saveMobileNo, saveFuneralDate, saveMemo);

        return result;
    }


    @Transactional
    public String stopMember(String ucode) {
        String result = memberRepositoryCustom.stopMember(ucode);
        return result;
    }

    @Transactional
    public String restoreMember(String ucode) {
        String result = memberRepositoryCustom.restoreMember(ucode);
        return result;
    }


    @Transactional
    public String deleteMember(String ucode, String memorialCode, String ptLinkCode) {

        fileService.deleteMemberDirectory(ptLinkCode, memorialCode);

        memberRepositoryCustom.deleteMemberPhoto(memorialCode);

        String result = memberRepositoryCustom.deleteMember(ucode);


        return result;
    }



    public MemberDto getMemberSttus(String ptLinkCode, String memorialCode) throws Exception {
        MemberDto dto = memberRepositoryCustom.getMemberSttus(ptLinkCode, memorialCode);

        String fd = dto.getFuneralDate() != null ? commonUtill.dateFormatter(dto.getFuneralDate()) : "미업로드";
        String mobileNo = dto.getMobileNo() != null ? EncryptionUtil.decryption(dto.getMobileNo()) : "-";

        dto.setFuneralDateStr(fd);
        dto.setMobileNo(mobileNo);

        return dto;
    }


    public List<FileDto> getThumbImg(String memorialCode) {
        List<FileDto> list = memberRepositoryCustom.getThumbImg(memorialCode);

        return list;
    }


    public Map<String, Object> getUploadImg(String memorialCode) throws Exception {
        List<FileDto> list = memberRepositoryCustom.getThumbImg(memorialCode);

        MemberDto memberDto = memberRepositoryCustom.getSaveMemberInfo(memorialCode);

        memberDto.setFuneralDateStr(memberDto.getFuneralDate() != null ? memberDto.getFuneralDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        memberDto.setMobileNo(EncryptionUtil.decryption(memberDto.getMobileNo()));

        Map<String, Object> map = new HashMap<>();
        map.put("imgList", list);
        map.put("memberDto", memberDto);

        return map;
    }


    public String makeSignature(String serviceId, String aKey, String sKey, Long time) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, java.security.InvalidKeyException {

        System.out.println("time ============================================> " + time);

        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/"+ serviceId +"/messages";
        String timestamp = time.toString();
        String accessKey = aKey;
        String secretKey = sKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    public List<FileDto> getUploadPhoto(String memorialCode) {
        if (!StringUtils.hasText(memorialCode)) return null;

        return memberRepositoryCustom.getUploadPhoto(memorialCode);
    }

    @Transactional
    public void savePetName(String petName, String memorialCode) {
         memberRepositoryCustom.savePetName(petName, memorialCode);
    }

    @Transactional
    public void saveSkinId(String skinId, String memorialCode) {
        memberRepositoryCustom.saveSkinId(skinId, memorialCode);
    }

    @Transactional
    public void saveMobileNo(String mobileNo, String memorialCode) throws Exception {
        memberRepositoryCustom.saveMobileNo(EncryptionUtil.encryption(mobileNo), memorialCode);
    }

    @Transactional
    public void saveFuneralDate(String funeralDateText, String memorialCode) {
        LocalDateTime localDateTime = LocalDateTime.parse(funeralDateText + "T00:00:00");
        memberRepositoryCustom.saveFuneralDate(localDateTime, memorialCode);
    }


    @Transactional
    public void saveTerms1(boolean terms1, String memorialCode) {
        String saveTerms1 = "1";
        if (!terms1) saveTerms1 = "0";
        memberRepositoryCustom.saveTerms1(saveTerms1, memorialCode);
    }

    @Transactional
    public void saveTerms2(boolean terms2, String memorialCode) {
        String saveTerms2 = "1";
        if (!terms2) saveTerms2 = "0";
        memberRepositoryCustom.saveTerms2(saveTerms2, memorialCode);
    }

}
