package com.aditya2254.ecommerceapp.productservice.service;

import com.aditya2254.ecommerceapp.productservice.dto.ImageResponse;
import com.aditya2254.ecommerceapp.productservice.entity.Product;
import com.aditya2254.ecommerceapp.productservice.exceptions.ProductNotFoundException;
import com.aditya2254.ecommerceapp.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling product operations with image integration.
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageService productImageService;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductImageService productImageService) {
        this.productRepository = productRepository;
        this.productImageService = productImageService;
    }

    /**
     * Get all products without images.
     *
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get all products with their images.
     *
     * @return List of all products and a map of product IDs to their images
     */
    public Map<String, Object> getAllProductsWithImages() {
        List<Product> products = productRepository.findAll();
        Map<Long, List<ImageResponse>> productImages = new HashMap<>();

        // Fetch images for each product
        for (Product product : products) {
            List<ImageResponse> images = productImageService.list(product.getId());
            if (!images.isEmpty()) {
                productImages.put(product.getId(), images);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("images", productImages);
        return result;
    }

    /**
     * Get a product by ID without images.
     *
     * @param id Product ID
     * @return Optional containing the product if found
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get a product by ID with its images.
     *
     * @param id Product ID
     * @return Map containing the product and its images
     * @throws ProductNotFoundException if the product is not found
     */
    public Map<String, Object> getProductWithImagesById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        List<ImageResponse> images = productImageService.list(id);

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("images", images);
        return result;
    }

    /**
     * Save a product.
     *
     * @param product Product to save
     * @return Saved product
     */
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Delete a product and all its images.
     *
     * @param id Product ID
     * @throws ProductNotFoundException if the product is not found
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);

        // Get all images for the product
        List<ImageResponse> images = productImageService.list(id);

        // Delete each image
        for (ImageResponse image : images) {
            productImageService.delete(id, image.id());
        }

        // Delete the product
        productRepository.delete(product);
    }

    /**
     * Update product stock.
     *
     * @param id Product ID
     * @param stock New stock value
     * @return Updated product
     * @throws ProductNotFoundException if the product is not found
     */
    @Transactional
    public Product updateStock(Long id, int stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(ProductNotFoundException::new);
        
        product.setStock(stock);
        return productRepository.save(product);
    }

    /**
     * Reserve product inventory.
     *
     * @param productId Product ID
     * @param quantity Quantity to reserve
     * @throws ProductNotFoundException if the product is not found
     * @throws com.aditya2254.ecommerceapp.productservice.exceptions.InsufficientStockException if there is not enough stock
     */
    @Transactional
    public void reserveProductInventory(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        
        if (product.getStock() < quantity) {
            throw new com.aditya2254.ecommerceapp.productservice.exceptions.InsufficientStockException();
        }
        
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    /**
     * Roll back inventory reservation.
     *
     * @param productId Product ID
     * @param quantity Quantity to roll back
     * @throws ProductNotFoundException if the product is not found
     */
    @Transactional
    public void rollBackInventory(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }
}