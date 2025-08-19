package com.aditya2254.ecommerceapp.productservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

    @Entity
    @Table(name = "product_images")
    public class ProductImage {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "product_id", nullable = false)
        private Long productId;

        @Column(name = "object_key", nullable = false, length = 512, unique = true)
        private String objectKey;

        @Column(nullable = false)
        private String filename;

        @Column(name = "content_type", nullable = false, length = 100)
        private String contentType;

        @Column(name = "size_bytes", nullable = false)
        private long sizeBytes;

        @Column(name = "checksum_sha256", nullable = false, length = 64)
        private String checksumSha256;

        private Integer widthPx;
        private Integer heightPx;

        @Column(name = "is_primary", nullable = false)
        private boolean primary;

        @Column(name = "order_index", nullable = false)
        private int orderIndex;

        @Column(name = "created_at", nullable = false)
        private Instant createdAt = Instant.now();

        public ProductImage() {
        }

        public ProductImage(Long id, Long productId, String objectKey, String filename, String contentType, long sizeBytes, String checksumSha256, Integer widthPx, Integer heightPx, boolean primary, int orderIndex, Instant createdAt) {
            this.id = id;
            this.productId = productId;
            this.objectKey = objectKey;
            this.filename = filename;
            this.contentType = contentType;
            this.sizeBytes = sizeBytes;
            this.checksumSha256 = checksumSha256;
            this.widthPx = widthPx;
            this.heightPx = heightPx;
            this.primary = primary;
            this.orderIndex = orderIndex;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public String getChecksumSha256() {
            return checksumSha256;
        }

        public void setChecksumSha256(String checksumSha256) {
            this.checksumSha256 = checksumSha256;
        }

        public Integer getWidthPx() {
            return widthPx;
        }

        public void setWidthPx(Integer widthPx) {
            this.widthPx = widthPx;
        }

        public Integer getHeightPx() {
            return heightPx;
        }

        public void setHeightPx(Integer heightPx) {
            this.heightPx = heightPx;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public void setOrderIndex(int orderIndex) {
            this.orderIndex = orderIndex;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "ProductImage{" +
                    "id=" + id +
                    ", productId=" + productId +
                    ", objectKey='" + objectKey + '\'' +
                    ", filename='" + filename + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", sizeBytes=" + sizeBytes +
                    ", checksumSha256='" + checksumSha256 + '\'' +
                    ", widthPx=" + widthPx +
                    ", heightPx=" + heightPx +
                    ", primary=" + primary +
                    ", orderIndex=" + orderIndex +
                    ", createdAt=" + createdAt +
                    '}';
        }
    }
