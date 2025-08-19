package com.aditya2254.ecommerceapp.productservice.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.aditya2254.ecommerceapp.productservice.dto.ImageResponse;
import com.aditya2254.ecommerceapp.productservice.dto.UploadImageResponse;
import com.aditya2254.ecommerceapp.productservice.entity.ProductImage;
import com.aditya2254.ecommerceapp.productservice.repositories.ProductImageRepository;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * ProductImageService - handles upload/list/delete + S3 interactions.
 *
 * Assumptions:
 * - ProductImage, ProductImageRepository, ImageResponse and UploadImageResponse exist in your codebase.
 * - S3Client is configured and injected (AWS SDK v2).
 */
@Service
public class ProductImageService {

    private final S3Client s3;
    private final ProductImageRepository repo;

    @Value("${app.images.s3.bucket}")
    private String bucket;

    @Value("${app.images.s3.public-base-url:}")
    private String publicBaseUrl;

    @Value("${app.images.s3.signed-url-ttl:PT15M}")
    private Duration signedTtl;

    public ProductImageService(S3Client s3, ProductImageRepository repo) {
        this.s3 = s3;
        this.repo = repo;
    }

    public List<ImageResponse> list(Long productId) {
        return repo.findByProductIdOrderByOrderIndexAscIdAsc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    
    /**
     * Sets a specific image as the primary image for a product.
     * Any existing primary image for the product will be set to non-primary.
     *
     * @param productId The ID of the product
     * @param imageId The ID of the image to set as primary
     * @return The updated image response
     * @throws NoSuchElementException if the image is not found
     * @throws IllegalArgumentException if the image does not belong to the product
     */
    @Transactional
    public ImageResponse setPrimaryImage(Long productId, Long imageId) {
        ProductImage img = repo.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("Image not found"));

        if (!Objects.equals(img.getProductId(), productId)) {
            throw new IllegalArgumentException("Image does not belong to product");
        }

        // If this image is already primary, no need to do anything
        if (img.isPrimary()) {
            return toResponse(img);
        }

        // Find the current primary image (if any) and set it to non-primary
        List<ProductImage> images = repo.findByProductIdOrderByOrderIndexAscIdAsc(productId);
        for (ProductImage image : images) {
            if (image.isPrimary()) {
                image.setPrimary(false);
                repo.save(image);
                break;
            }
        }

        // Set the new image as primary
        img.setPrimary(true);
        repo.save(img);

        return toResponse(img);
    }

    @Transactional
    public UploadImageResponse upload(Long productId, List<MultipartFile> files) {
        List<ImageResponse> result = new ArrayList<>();
        int baseIndex = (int) repo.countByProductId(productId);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            validateFile(f);

            String ext = extension(f.getOriginalFilename());
            String key = "products/" + productId + "/" + UUID.randomUUID() + ext;

            String checksum = sha256Hex(f);
            Integer[] wh = readDimensions(f);

            putObject(key, f);

            ProductImage entity = new ProductImage();
            entity.setProductId(productId);
            entity.setObjectKey(key);
            entity.setFilename(Optional.ofNullable(f.getOriginalFilename()).orElse("image" + ext));
            entity.setContentType(f.getContentType());
            entity.setSizeBytes(f.getSize());
            entity.setChecksumSha256(checksum);
            entity.setWidthPx(wh[0]);
            entity.setHeightPx(wh[1]);
            entity.setPrimary(baseIndex == 0 && i == 0); // first image becomes primary if none
            entity.setOrderIndex(baseIndex + i);

            ProductImage saved = repo.save(entity);
            result.add(toResponse(saved));
        }
        return new UploadImageResponse(productId, result);
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        ProductImage img = repo.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("image not found"));

        if (!Objects.equals(img.getProductId(), productId)) {
            throw new IllegalArgumentException("image does not belong to product");
        }

        // delete from S3
        try {
            DeleteObjectRequest delReq = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(img.getObjectKey())
                    .build();
            s3.deleteObject(delReq);
        } catch (S3Exception e) {
            // log or rethrow depending on your policy
            throw new RuntimeException("failed to delete object from S3: " + e.getMessage(), e);
        }

        // delete from repo
        repo.delete(img);

        // if deleted image was primary, set another image as primary (first by order index)
        if (img.isPrimary()) {
            List<ProductImage> remaining = repo.findByProductIdOrderByOrderIndexAscIdAsc(productId);
            if (!remaining.isEmpty()) {
                ProductImage first = remaining.get(0);
                first.setPrimary(true);
                repo.save(first);
            }
        }
    }

    /* ---------------------- helper methods ---------------------- */

    private void validateFile(MultipartFile f) {
        if (f == null || f.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }
        if (f.getSize() <= 0) {
            throw new IllegalArgumentException("file size is zero");
        }
        String ct = f.getContentType();
        if (ct == null || (!ct.startsWith("image/") && !ct.equals("application/octet-stream"))) {
            // allow application/octet-stream for some uploads, but prefer image/*
            throw new IllegalArgumentException("unsupported content type: " + ct);
        }
        // you can add extra checks here like max size, allowed extensions etc.
    }

    private String extension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return "";
        return filename.substring(idx).toLowerCase();
    }

    private String sha256Hex(MultipartFile f) {
        try (InputStream in = f.getInputStream();
             DigestInputStream din = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            byte[] buf = new byte[8192];
            while (din.read(buf) != -1) {
                // digest is updated by reading
            }
            byte[] digest = din.getMessageDigest().digest();
            return Hex.encodeHexString(digest);
        } catch (Exception e) {
            throw new RuntimeException("unable to compute sha256", e);
        }
    }

    /**
     * Reads image dimensions. Returns {width, height}. Returns {0,0} if cannot read.
     */
    private Integer[] readDimensions(MultipartFile f) {
        try (InputStream in = f.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) return new Integer[] { 0, 0 };
            return new Integer[] { img.getWidth(), img.getHeight() };
        } catch (IOException e) {
            return new Integer[] { 0, 0 };
        }
    }

    private void putObject(String key, MultipartFile f) {
        try (InputStream in = f.getInputStream()) {
            PutObjectRequest por = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(Optional.ofNullable(f.getContentType()).orElse("application/octet-stream"))
                    .contentLength(f.getSize())
                    .build();

            s3.putObject(por, RequestBody.fromInputStream(in, f.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("failed to read file for upload", e);
        } catch (S3Exception e) {
            throw new RuntimeException("s3 putObject failed: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private ImageResponse toResponse(ProductImage img) {
        String url = generateUrl(img.getObjectKey());
        ImageResponse r = new ImageResponse(
                img.getId(),
                url,
                img.isPrimary(),
                img.getOrderIndex(),
                img.getContentType(),
                img.getSizeBytes(),
                img.getWidthPx(),
                img.getHeightPx());
        return r;
    }

    /**
     * Generate URL for object:
     * - if publicBaseUrl configured, return publicBaseUrl + "/" + key
     * - otherwise try to generate a presigned GET URL using S3Presigner with the configured TTL.
     */
    private String generateUrl(String key) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                    : publicBaseUrl;
            return base + "/" + key;
        }

        // Attempt to presign using default S3Presigner (relies on environment configuration)
        try (S3Presigner presigner = S3Presigner.create()) {
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                    .getObjectRequest(getReq)
                    .signatureDuration(signedTtl != null ? signedTtl : Duration.ofMinutes(15))
                    .build();

            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignReq);
            URL url = presigned.url();
            return url.toString();
        } catch (Exception e) {
            // As a last fallback (if presigner cannot be created), attempt to build a URL via S3Utilities
            try {
                URL url = s3.utilities().getUrl(builder -> builder.bucket(bucket).key(key).build());
                return url.toString();
            } catch (Exception ex) {
                throw new RuntimeException("failed to generate URL for object key: " + key, ex);
            }
        }
    }
}
