package com.aditya2254.ecommerceapp.productservice;

import com.aditya2254.ecommerceapp.productservice.dto.ImageResponse;
import com.aditya2254.ecommerceapp.productservice.dto.UploadImageResponse;
import com.aditya2254.ecommerceapp.productservice.response.CustomResponse;
import com.aditya2254.ecommerceapp.productservice.service.ProductImageService;
import com.aditya2254.ecommerceapp.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controller for handling product image operations.
 */
@RestController
@RequestMapping("/api/products")
public class ProductImageController {

    private final ProductImageService imageService;
    private final ProductService productService;

    @Autowired
    public ProductImageController(ProductImageService imageService, ProductService productService) {
        this.imageService = imageService;
        this.productService = productService;
    }

    /**
     * Get all images for a product.
     *
     * @param productId Product ID
     * @return List of image responses
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ImageResponse>> getProductImages(@PathVariable Long productId) {
        // Verify product exists
        productService.getProductById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        List<ImageResponse> images = imageService.list(productId);
        return ResponseEntity.ok(images);
    }

    /**
     * Upload images for a product.
     *
     * @param productId Product ID
     * @param files Images to upload
     * @return Upload response with details of uploaded images
     */
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadImageResponse> uploadProductImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files) {
        
        // Verify product exists
        productService.getProductById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UploadImageResponse response = imageService.upload(productId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete an image for a product.
     *
     * @param productId Product ID
     * @param imageId Image ID
     * @return Success response
     */
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<CustomResponse> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        
        try {
            imageService.delete(productId, imageId);
            return ResponseEntity.ok(new CustomResponse<>("Image deleted successfully"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new CustomResponse<>(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CustomResponse<>(e.getMessage()));
        }
    }

    /**
     * Set an image as the primary image for a product.
     *
     * @param productId Product ID
     * @param imageId Image ID
     * @return Updated image response
     */
    @PutMapping("/{productId}/images/{imageId}/primary")
    public ResponseEntity<ImageResponse> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        
        try {
            ImageResponse response = imageService.setPrimaryImage(productId, imageId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}