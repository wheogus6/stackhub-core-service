package core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "admin")
public class Admin {

    @Id
    @Column(name = "acode")
    private String acode;

    @Column(name = "aid")
    private String aid;

    @Column(name = "pwd")
    private String pwd;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "role")
    private String role;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "upd_date")
    private LocalDateTime updDate;
}
