package core.dto;

import lombok.Data;

@Data
public class ImageResponse {

    private String downloadUrl;

    public ImageResponse(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}


