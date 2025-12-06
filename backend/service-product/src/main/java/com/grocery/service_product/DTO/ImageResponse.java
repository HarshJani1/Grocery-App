package com.grocery.service_product.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private String imageBase64;
    private String imageType;

    public ImageResponse(String imageBase64, String imageType) {
        this.imageBase64 = imageBase64;
        this.imageType = imageType;
    }

    public ImageResponse() {}

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}
