package com.aditya2254.ecommerceapp.ordersservice.repositories;

import com.aditya2254.ecommerceapp.ordersservice.entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, Long> {

    public List<CartItems> findByUserId(String userId);

    public List<CartItems> findByUserIdAndProductId(String userId, Long productId);

    @Transactional
    public void deleteByUserId(String userId);
}
