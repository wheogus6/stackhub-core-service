package core.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminDto {

    public String acode;

    public String aid;

    public String pwd;

    public String mobileNo;

    public String role;

    public LocalDateTime regDate;

    public LocalDateTime updDate;

}
