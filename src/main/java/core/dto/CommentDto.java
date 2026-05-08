package core.dto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;

    private String memorialCode;

    private String name;

    private String comment;

    private LocalDateTime regDate;
}
