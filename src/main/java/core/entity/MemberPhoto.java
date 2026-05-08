package core.entity;

import jakarta.persistence.*;
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
@Table(name = "member_photo")
public class MemberPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private Long id;

    @Column(name = "memorial_code")
    private String memorialCode;

    @Column(name = "file_dir")
    private String fileDir;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "type")
    private String type;

    @Column(name = "upd_date")
    private LocalDateTime updDate;

    @Column(name = "back_up_sttus")
    private String backUpSttus;

}
