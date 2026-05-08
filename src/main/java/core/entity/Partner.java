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
@Table(name = "partner")
public class Partner {

    @Id
    @Column(name = "pt_code")
    private String ptCode;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "tel_no")
    private String telNo;

    @Column(name = "pt_name")
    private String ptName;

    @Column(name = "addr")
    private String addr;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "memo")
    private String memo;

    @Column(name = "pt_link_code")
    private String ptLinkCode;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "upd_date")
    private LocalDateTime updDate;

    @Column(name = "id")
    private String id;

    @Column(name = "pwd")
    private String pwd;

    @Column(name = "sttus")
    private String sttus;

    @Column(name = "pt_logo_img")
    private String ptLogoImg;
}
