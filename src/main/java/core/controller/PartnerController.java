package core.controller;

import core.dto.MemberDto;
import core.service.MemberService;
import core.service.PartnerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/gday/partner")
public class PartnerController {


    @Autowired
    PartnerService partnerService;
    @Autowired
    MemberService memberService;



    @GetMapping(value = {"/", ""})
    public String partnerAdmin(HttpSession session) {
        if (session.getAttribute("ptLinkCode") == null) {
            return "redirect:/gday/partner/login";
        } else {
            return "partner/partnerAdmin";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "partner/partnerAdminLogin";
    }

    @PostMapping("/loginCheck")
    @ResponseBody
    public String loginCheck(String id, String pwd) throws Exception {
        String result = partnerService.loginCheck(id, pwd);
        return result;
    }

    @PostMapping("/logout")
    @ResponseBody
    public String logout() throws Exception {
        String result = partnerService.logout();
        return result;
    }


    @PostMapping("/getPartnerAdminMemberDtl")
    @ResponseBody
    public MemberDto getPartnerAdminMemberDtl(String memorialCode) throws Exception {
        MemberDto dto = memberService.getPartnerAdminMemberDtl(memorialCode);

        return dto;
    }


}
