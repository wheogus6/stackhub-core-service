package core.dto;

import lombok.Data;

@Data
public class PageDto {

    public Object rows;

    public int page;

    public int pageSize;

    public long totalSize;

}
