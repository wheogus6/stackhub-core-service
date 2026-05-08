package core.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AdminHomeController {

    @RequestMapping("/")
    public String goLoginPage(HttpSession session) {
        // 세션 체크해서 아무것도 없으면 로그인 페이지로 이동
        if (session.getAttribute("acode") == null) {
            return "redirect:/admin/login/login";
        } else {
            return "admin/member";
        }
    }


    @PostMapping("/checkSession")
    @ResponseBody
    public String checkSession(HttpSession session) {
        if (session.getAttribute("acode") == null) {
            return "90";
        } else {
            return "00";
        }
    }



}
