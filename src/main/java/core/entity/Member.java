package core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
public class Member {

    @Id
    @Column(name = "member_code")
    private String memberCode;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "member_name")
    private String memberName;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "auth_code")
    private String authCode;

    @Column(name = "reg_date")
    private LocalDateTime regDate;



}
