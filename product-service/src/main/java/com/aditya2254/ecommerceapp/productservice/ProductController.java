package com.aditya2254.ecommerceapp.productservice;

import com.aditya2254.ecommerceapp.productservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.productservice.dto.StockUpdateRequest;
import com.aditya2254.ecommerceapp.productservice.entity.Product;
import com.aditya2254.ecommerceapp.productservice.exceptions.InsufficientStockException;
import com.aditya2254.ecommerceapp.productservice.exceptions.ProductNotFoundException;
import com.aditya2254.ecommerceapp.productservice.service.ProductService;
import com.aditya2254.ecommerceapp.productservice.response.CustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for handling product operations.
 */
@RestController
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products without images.
     *
     * @return List of all products
     */
    @GetMapping(path = "/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Get all products with their images.
     *
     * @return Map containing products and their images
     */
    @GetMapping(path = "/products/with-images")
    public ResponseEntity<Map<String, Object>> getProductsWithImages() {
        return ResponseEntity.ok(productService.getAllProductsWithImages());
    }

    /**
     * Get a product by ID without images.
     *
     * @param id Product ID
     * @return Product if found
     */
    @GetMapping(path = "/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a product by ID with its images.
     *
     * @param id Product ID
     * @return Map containing the product and its images
     */
    @GetMapping(path = "/products/{id}/with-images")
    public ResponseEntity<Map<String, Object>> getProductWithImages(@PathVariable Long id) {
        try {
            Map<String, Object> result = productService.getProductWithImagesById(id);
            return ResponseEntity.ok(result);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add a new product.
     *
     * @param product Product to add
     * @return Success response with the added product
     */
    @PostMapping(path = "/api/products")
    public ResponseEntity<CustomResponse> addProduct(@RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return buildCustomResponse("Product added successfully", savedProduct, HttpStatus.CREATED);
    }

    /**
     * Delete a product and all its images.
     *
     * @param id Product ID
     * @return Success response
     */
    @DeleteMapping(path = "/api/products/{id}")
    public ResponseEntity<CustomResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return buildCustomResponse("Product deleted successfully", HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return buildCustomResponse("Product not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update product stock.
     *
     * @param id Product ID
     * @param request Stock update request
     * @return Updated product
     */
    @PutMapping("/api/products/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        try {
            Product updatedProduct = productService.updateStock(id, request.getStock());
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reserve inventory for products.
     *
     * @param request Inventory reservation request
     * @return Success response
     */
    @PostMapping("/api/products/reserve")
    public ResponseEntity<CustomResponse> reserveInventory(@RequestBody InventoryReservationRequest request) {
        if (request.items().isEmpty()) {
            return buildCustomResponse("No items to reserve", HttpStatus.BAD_REQUEST);
        }
        Long productId = null;
        try {
            for (Map.Entry<Long, Integer> entry : request.items().entrySet()) {
                Long key = entry.getKey();
                productId = key;
                Integer value = entry.getValue();
                productService.reserveProductInventory(key, value);
            }
            return buildCustomResponse("Success: Inventory reserved successfully", HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            rollbackFailedItems(request.items().entrySet(), productId);
            return buildCustomResponse("Product not found for productId: %d".formatted(productId), HttpStatus.NOT_FOUND);
        } catch (InsufficientStockException e) {
            rollbackFailedItems(request.items().entrySet(), productId);
            return buildCustomResponse("Error: Not enough stock available for productId: %d".formatted(productId), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Roll back inventory reservation.
     *
     * @param request Inventory rollback request
     * @return Success response
     */
    @PostMapping("/api/products/rollback")
    public ResponseEntity<CustomResponse> rollbackInventory(@RequestBody InventoryReservationRequest request) {
        if (request.items().isEmpty()) {
            return buildCustomResponse("No items to rollback", HttpStatus.BAD_REQUEST);
        }
        try {
            for (Map.Entry<Long, Integer> entry : request.items().entrySet()) {
                productService.rollBackInventory(entry.getKey(), entry.getValue());
            }
            return buildCustomResponse("Success: Inventory rolled back successfully", HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return buildCustomResponse("Error: Product not found for rollback", HttpStatus.NOT_FOUND);
        }
    }

    private void rollbackFailedItems(Set<Map.Entry<Long, Integer>> entries, Long productId) {
        for (Map.Entry<Long, Integer> entry : entries) {
            if (productId.equals(entry.getKey())) {
                break;
            }
            productService.rollBackInventory(entry.getKey(), entry.getValue());
        }
    }

    private <T> ResponseEntity<CustomResponse> buildCustomResponse(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status).body(new CustomResponse<>(message, data));
    }

    private ResponseEntity<CustomResponse> buildCustomResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new CustomResponse<>(message));
    }
}
