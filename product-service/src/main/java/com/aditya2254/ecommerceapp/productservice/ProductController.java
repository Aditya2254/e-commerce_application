package com.aditya2254.ecommerceapp.productservice;

import com.aditya2254.ecommerceapp.productservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.productservice.dto.StockUpdateRequest;
import com.aditya2254.ecommerceapp.productservice.entity.Product;
import com.aditya2254.ecommerceapp.productservice.exceptions.InsufficientStockException;
import com.aditya2254.ecommerceapp.productservice.exceptions.ProductNotFoundException;
import com.aditya2254.ecommerceapp.productservice.repositories.ProductRepository;
import com.aditya2254.ecommerceapp.productservice.response.CustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @GetMapping(path = "/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping(path = "/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        /*Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return buildCustomResponse("Success: Product found", product.get(), HttpStatus.OK);
        } else {
            return buildCustomResponse("Error: Product not found for productId: %d".formatted(id), HttpStatus.NOT_FOUND);
        }*/
    }


    @PostMapping(path = "/products")
    public ResponseEntity<CustomResponse> addProduct(@RequestBody Product product) {
        productRepository.save(product);
        return buildCustomResponse("Product added successfully", product, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/products/{id}")
    public ResponseEntity<CustomResponse> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return buildCustomResponse("Product deleted successfully", HttpStatus.OK);
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestBody StockUpdateRequest request) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setStock(request.getStock());
                    productRepository.save(existingProduct);
                    return ResponseEntity.ok(existingProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/products/reserve")
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
                reserveProductInventory(key, value);
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

    @PostMapping("/products/rollback")
    public ResponseEntity<CustomResponse> rollbackInventory(@RequestBody InventoryReservationRequest request) {
        if (request.items().isEmpty()) {
            return buildCustomResponse("No items to rollback", HttpStatus.BAD_REQUEST);
        }
        try {
            for (Map.Entry<Long, Integer> entry : request.items().entrySet()) {
                rollBackInventory(entry.getKey(), entry.getValue());
            }
            return buildCustomResponse("Success: Inventory rolled back successfully", HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return buildCustomResponse("Error: Product not found for rollback", HttpStatus.NOT_FOUND);
        }
    }

    private void reserveProductInventory(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        if (product.getStock() < quantity) {
            throw new InsufficientStockException();
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private void rollbackFailedItems(Set<Map.Entry<Long, Integer>> entries, Long productId) {
        for (Map.Entry<Long, Integer> entry : entries){
            if (productId.equals(entry.getKey())) {
                break;
            }
            rollBackInventory(entry.getKey(), entry.getValue());
        }
    }

    private void rollBackInventory(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    private <T> ResponseEntity<CustomResponse> buildCustomResponse(String message, T data, HttpStatus status) {
        return ResponseEntity.status(status).body(new CustomResponse<>(message, data));
    }

    private ResponseEntity<CustomResponse> buildCustomResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new CustomResponse<>(message));
    }

}
