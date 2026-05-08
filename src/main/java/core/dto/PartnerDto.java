package core.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class PartnerDto {

    public String ptCode;

    public String id;

    public String pwd;

    public String mobileNo;

    public String telNo;

    public String ptName;

    public String addr;

    public String homepage;

    public String memo;

    public String ptLinkCode;

    public String sttus;

    public String ptLogoImg;

    public MultipartFile ptLogoImgFile;

    public LocalDateTime regDate;

    public LocalDateTime updDate;

}
