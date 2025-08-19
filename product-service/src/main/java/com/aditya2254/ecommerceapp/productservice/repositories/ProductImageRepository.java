package com.aditya2254.ecommerceapp.productservice.repositories;
import com.aditya2254.ecommerceapp.productservice.entity.ProductImage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByOrderIndexAscIdAsc(Long productId);
    long countByProductId(Long productId);
}
