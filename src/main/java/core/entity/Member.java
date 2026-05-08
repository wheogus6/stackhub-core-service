package core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@Table(name = "member")
public class Member {

    @Id
    @Column(name = "ucode")
    private String ucode;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "uname")
    private String uname;

    @Column(name = "funeral_date")
    private LocalDateTime funeralDate;

    @Column(name = "pt_link_code")
    private String ptLinkCode;

    @Column(name = "create_user_url")
    private String createUserUrl;

    @Column(name = "upd_date")
    private LocalDateTime updDate;

    @Column(name = "memorial_code")
    private String memorialCode;

    @Column(name = "pet_name")
    private String petName;

//    @Column(name = "pt_homepage")
//    private String ptHomepage;

    @Column(name = "funeral_url")
    private String funeralUrl;

    @Column(name = "status")
    private String status;

    @Column(name = "expDate")
    private LocalDateTime expDate;

    @Column(name = "limit_date")
    private Integer limitDate;

    @Column(name = "terms_agree_date")
    private LocalDateTime termsAgreeDate;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "memo")
    private String memo;

    @Column(name = "member_sttus")
    private String memberSttus;

    @Column(name = "skin_id")
    private String skinId;

    @Column(name = "terms1")
    private String terms1;

    @Column(name = "terms2")
    private String terms2;

}
