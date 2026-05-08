package core.repository;


import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import core.dto.AdminDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import static core.entity.QAdmin.admin;


@Repository
public class AdminRepositoryCustom {

   private final JPAQueryFactory query;

   @Autowired
   public AdminRepositoryCustom(JPAQueryFactory query) {
       this.query = query;
   }


    public AdminDto loginCheck(String aid) {
        AdminDto dto = query.select(Projections.bean(AdminDto.class,
                        admin.acode,
                        admin.aid,
                        admin.pwd))
                .from(admin)
                .where(admin.aid.eq(aid))
                .fetchOne();

        return dto;

    }



   //어드민 아이디 비번 등 암호화 업데이트
   public String update(String pwd, String mobileNo, LocalDateTime now) {
       query.update(admin)
               .set(admin.pwd, pwd)
               .set(admin.mobileNo, mobileNo)
               .set(admin.regDate, now)
               .where(admin.acode.eq("A000001"))
               .execute();

       return "00";
   }




}
