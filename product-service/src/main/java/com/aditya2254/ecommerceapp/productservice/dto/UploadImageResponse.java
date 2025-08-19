package com.aditya2254.ecommerceapp.productservice.dto;

import java.util.List;

public record UploadImageResponse(Long productId, List<ImageResponse> images) {

    public UploadImageResponse(Long productId, List<ImageResponse> images) {
        this.productId = productId;
        this.images = images;
    }

    @Override
    public Long productId() {
        return productId;
    }

    @Override
    public List<ImageResponse> images() {
        return images;
    }
}
