package core.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/admin/login")
public class AdminLoginController {



    @GetMapping("/login")
    public String login() {
        return "admin/adminLogin";
    }








}
