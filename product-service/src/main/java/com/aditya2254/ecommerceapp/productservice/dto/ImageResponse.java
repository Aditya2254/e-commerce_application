package com.aditya2254.ecommerceapp.productservice.dto;

public record ImageResponse(
        Long id,
        String url,
        boolean primary,
        int orderIndex,
        String contentType,
        long sizeBytes,
        Integer widthPx,
        Integer heightPx
) {
    public ImageResponse(Long id, String url, boolean primary, int orderIndex, String contentType, long sizeBytes, Integer widthPx, Integer heightPx) {
        this.id = id;
        this.url = url;
        this.primary = primary;
        this.orderIndex = orderIndex;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public boolean primary() {
        return primary;
    }

    @Override
    public int orderIndex() {
        return orderIndex;
    }

    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public long sizeBytes() {
        return sizeBytes;
    }

    @Override
    public Integer widthPx() {
        return widthPx;
    }

    @Override
    public Integer heightPx() {
        return heightPx;
    }
}
