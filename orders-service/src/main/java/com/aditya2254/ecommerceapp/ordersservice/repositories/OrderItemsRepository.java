package com.aditya2254.ecommerceapp.ordersservice.repositories;

import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
}
