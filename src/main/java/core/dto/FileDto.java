package core.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class FileDto {

    private MultipartFile file;

    private String fileId;

    private String ptLinkCode;

    private String memorialCode;

    private String fileDir;

    private LocalDateTime regDate;

    private String type;

    private String backUpSttus;

    private Long id;

    private Integer width;

    private Integer height;



    public String petName;
    public String skinId;

    public String mobileNo;

    public MultipartFile m01File;

    public MultipartFile file01;

    public MultipartFile file02;

    public MultipartFile file03;

    public MultipartFile file04;

    public MultipartFile file05;

    public Integer rotationM01;
    public Integer rotation01;
    public Integer rotation02;
    public Integer rotation03;
    public Integer rotation04;
    public Integer rotation05;

    public Integer rotation;

    private LocalDateTime updDate;
    private String funeralDateStr;
    private LocalDateTime funeralDate;

    private LocalDateTime expDate;

    private Integer limit;

}
