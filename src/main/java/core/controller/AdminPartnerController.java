package core.controller;

import core.dto.PageDto;
import core.dto.PartnerDto;
import core.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/partner")
public class AdminPartnerController {

    @Autowired
    PartnerService partnerService;


    @GetMapping("/goPartnerPage")
    public String goMemberPage() {
        return "admin/partner";
    }

    @GetMapping("/goPartnerDtl")
    public String goPartnerDtl(String ptlCode, Model model) {
        model.addAttribute("ptlCode", ptlCode);
        return "admin/partnerDtl";
    }

    @PostMapping("/newPartner")
    @ResponseBody
    public String newPartner(PartnerDto dto) throws Exception {
        String result = partnerService.newPartner(dto);
        return result;
    }

    @PostMapping("/editPartner")
    @ResponseBody
    public String editPartner(PartnerDto dto) throws Exception {
        String result = partnerService.editPartner(dto);
        return result;
    }

    @PostMapping("/getPartnerList")
    @ResponseBody
    public Object getPartnerList(PartnerDto dto, Pageable pageable) throws Exception {
        Page<PartnerDto> list = partnerService.getPartnerList(dto, pageable);

        PageDto pd = new PageDto();
        pd.rows = list.getContent();
        pd.pageSize = list.getTotalPages();
        pd.page = list.getNumber();
        pd.totalSize = list.getTotalElements();

        return pd;
    }

    @PostMapping("/deletePartner")
    @ResponseBody
    public String deleteComment(String ptCode) throws Exception {
        String result = partnerService.deletePartner(ptCode);
        return result;
    }


    @PostMapping("/getPartnerDtl")
    @ResponseBody
    public PartnerDto getPartnerDtl(String ptlCode) throws Exception {
        PartnerDto dto = partnerService.getPartnerDtl(ptlCode);
        return dto;
    }


    @PostMapping("/getAllPartnerList")
    @ResponseBody
    public List<PartnerDto> getAllPartnerList() throws Exception {
        List<PartnerDto> list = partnerService.getAllPartnerList();
        return list;
    }
}
