package core.service;

import core.dto.PartnerDto;
import core.entity.Partner;
import core.repository.PartnerRepository;
import core.repository.PartnerRepositoryCustom;
import core.utill.CommonUtill;
import core.utill.EncryptionUtil;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.EncodingGroovyMethods.sha256;

@Service
public class PartnerService {

    @Autowired
    FileService fileService;
    @Autowired
    PartnerRepository partnerRepository;

    @Autowired
    PartnerRepositoryCustom partnerRepositoryCustom;

    @Autowired
    CommonUtill commonUtill;

    @Autowired
    private HttpSession session;

    //TODO : 나중에 폴더 저장 추가
    @Transactional
    public String newPartner(PartnerDto dto) throws Exception {

        long exists = partnerRepositoryCustom.existsByPId(dto.getId());

        String result = "00";

        if (exists >= 1) {
            result = "99";
        } else {
            String ptCode = commonUtill.createNewCode("partner");

            String ptLinkCode = commonUtill.createNewCode("partnerlink");

            String telNo = EncryptionUtil.encryption(dto.getTelNo());

            String ptName = StringEscapeUtils.escapeHtml4(dto.getPtName());

            String addr = StringEscapeUtils.escapeHtml4(dto.getAddr());

            String homePage = StringEscapeUtils.escapeHtml4(dto.getHomepage());

            String memo = StringEscapeUtils.escapeHtml4(dto.getMemo());

            LocalDateTime now = LocalDateTime.now();

            String pwd = sha256(dto.getPwd());

            Partner partner = new Partner();

            if (!dto.getPtLogoImgFile().isEmpty()) {
                MultipartFile img = dto.getPtLogoImgFile();
                String fileUrl = fileService.submitPartnerLogoImg(img, ptLinkCode);
                partner.setPtLogoImg(fileUrl);
            }

            partner.setPtCode(ptCode);
            partner.setPtLinkCode(ptLinkCode);
            partner.setTelNo(telNo);
            partner.setPtName(ptName);
            partner.setAddr(addr);
            partner.setHomepage(homePage);
            partner.setMemo(memo);
            partner.setRegDate(now);
            partner.setId(dto.getId());
            partner.setPwd(pwd);
            partner.setSttus("00");

            partnerRepository.save(partner);
        }

        return result;
    }


    @Transactional
    public String editPartner(PartnerDto dto) throws Exception {
        String result = "00";

        PartnerDto partnerDto = partnerRepositoryCustom.getPartnerDtl(dto.getPtLinkCode());


        if (!partnerDto.getId().equals(dto.getId())) {
            long existsId = partnerRepositoryCustom.existsByPId(dto.getId());
            if (existsId >= 1) {
                result = "99";
                return result;
            } else {
                dto.setId(StringEscapeUtils.escapeHtml4(dto.getId()));
            }
        } else {
            dto.setId(partnerDto.getId());
        }


        if (!partnerDto.getPtName().equals(dto.getPtName())) {
            long existsPtName = partnerRepositoryCustom.existsByPtName(dto.getPtName());
            if (existsPtName >= 1) {
                result = "98";
                return result;
            } else {
                String ptName = StringEscapeUtils.escapeHtml4(dto.getPtName());
                dto.setPtName(ptName);
            }
        } else {
            dto.setPtName(partnerDto.getPtName());
        }

        if (!dto.getPwd().equals("")) {
            dto.setPwd(sha256(dto.getPwd()));
        } else {
            dto.setPwd(partnerDto.getPwd());
        }

        String telNo = EncryptionUtil.encryption(dto.getTelNo());

        String addr = StringEscapeUtils.escapeHtml4(dto.getAddr());

        String homePage = StringEscapeUtils.escapeHtml4(dto.getHomepage());

        String memo = StringEscapeUtils.escapeHtml4(dto.getMemo());

        LocalDateTime now = LocalDateTime.now();

        dto.setTelNo(telNo);
        dto.setAddr(addr);
        dto.setHomepage(homePage);
        dto.setMemo(memo);
        dto.setUpdDate(now);


        if (!dto.getPtLogoImgFile().isEmpty()) {
            MultipartFile img = dto.getPtLogoImgFile();
            String fileUrl = fileService.submitPartnerLogoImg(img, dto.getPtLinkCode());
            dto.setPtLogoImg(fileUrl);
        } else {
            dto.setPtLogoImg(partnerDto.getPtLogoImg());
        }

        result = partnerRepositoryCustom.editPartner(dto);

        return result;
    }


    public Page<PartnerDto> getPartnerList(PartnerDto dto, Pageable pageable) throws Exception {
//        Page<PartnerDto> list = partnerRepositoryCustom.getPartnerList(dto, pageable);
//        for (PartnerDto partnerDto : list) {
//            String num = EncryptionUtil.decryption(partnerDto.getTelNo());
//            partnerDto.setTelNo(commonUtill.addDashForTelNo(num));
//        }
//        return list;
        int page = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        List<Map<String, Object>> list2 = partnerRepository.findPartners(dto, page, limit);

        List<PartnerDto> partnerDtos = list2.stream().map(map -> {
            String telNo = null;
            try {
                telNo = EncryptionUtil.decryption(map.get("telNo").toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            PartnerDto partnerDto = new PartnerDto();
            partnerDto.setTelNo(commonUtill.addDashForTelNo(telNo));
            partnerDto.setPtCode(map.get("ptCode").toString());
            partnerDto.setPtLinkCode(map.get("ptLinkCode").toString());
            partnerDto.setId(map.get("id").toString());
            partnerDto.setPtName(map.get("ptName").toString());
            partnerDto.setPtCode(map.get("ptCode").toString());
            return partnerDto;
        }).collect(Collectors.toList());


        long count = partnerRepository.findPartnersAdminCount(dto);

        return new PageImpl<>(partnerDtos, pageable, count);

    }

    public PartnerDto getPartnerDtl(String ptlCode) throws Exception {
        PartnerDto dto = partnerRepositoryCustom.getPartnerDtl(ptlCode);

        dto.setTelNo(EncryptionUtil.decryption(dto.getTelNo()));

        return dto;
    }

    public List<PartnerDto> getAllPartnerList() {
        List<PartnerDto> list = partnerRepositoryCustom.getAllPartnerList();
        return list;
    }

    @Transactional
    public String deletePartner(String ptCode) throws Exception {
        String result = partnerRepositoryCustom.deletePartner(ptCode);
        return result;
    }


    public String loginCheck(String id, String pwd) throws Exception {

        PartnerDto dto = partnerRepositoryCustom.loginCheck(id);

        String loginPwd = sha256(pwd);

        String result = "";

        if (dto != null) {
            String adminPwd = dto.getPwd();
            if (dto.getSttus().equals("00")) {
                if (loginPwd.equals(adminPwd)) {
                    // 로그인 성공
                    setPartnerLoginSession(dto);
                    result = "00";
                } else {
                    //비번 틀림
                    result = "99";
                }
            } else {
                result = "88";
            }
        } else {
            //없는 아이디
            result = "98";
        }
        return result;
    }

    public void setPartnerLoginSession(PartnerDto dto) {
        session.setAttribute("ptCode", dto.getPtCode());
        session.setAttribute("ptLinkCode", dto.getPtLinkCode());
        session.setAttribute("homepage", dto.getHomepage());
        session.setAttribute("ptName", dto.getPtName());
    }

    public String logout() {
        String result = "";
        try {
            session.invalidate();
            result = "00";
        } catch (Exception e) {
            result = "99";
        }
        return result;
    }

}
