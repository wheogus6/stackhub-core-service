package core.service;

import core.dto.AdminDto;
import core.repository.AdminRepository;
import core.repository.AdminRepositoryCustom;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static org.codehaus.groovy.runtime.EncodingGroovyMethods.sha256;

@Service
public class AdminService {

    @Autowired
    AdminRepository adminRepository;
    @Autowired
    AdminRepositoryCustom adminRepositoryCustom;

    @Autowired
    private HttpSession session;


    public String loginCheck(String id, String pwd) throws Exception {

        AdminDto dto = adminRepositoryCustom.loginCheck(id);

        String loginPwd = sha256(pwd);

        String result = "";

        if (dto != null) {
            String adminPwd = dto.getPwd();
            if (loginPwd.equals(adminPwd)) {
                // 로그인 성공
                setLoginSession(dto);
                result = "00";
            } else {
                //비번 틀림
                result = "99";
            }
        } else {
            //없는 아이디
            result = "98";
        }
        return result;
    }


    public void setLoginSession(AdminDto dto) {
        session.setAttribute("acode", dto.getAcode());
        session.setAttribute("aid", dto.getAid());
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
