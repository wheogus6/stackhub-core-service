package core.controller;

import core.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
@RequestMapping("/admin/login")
public class AdminLoginController {

    @Autowired
    AdminService adminServie;

    @GetMapping("/login")
    public String login() {
        return "admin/adminLogin";
    }

    @PostMapping("/loginCheck")
    @ResponseBody
    public String loginCheck(String id, String pwd) throws Exception {
        String result = adminServie.loginCheck(id, pwd);
        return result;
    }

    @PostMapping("/logout")
    @ResponseBody
    public String logout() {
        String result = adminServie.logout();
        return result;
    }





}
