package com.aditya2254.ecommerceapp.ordersservice.repositories;

import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

    public List<OrderItems> findByOrderId(Long orderId);

    public void deleteByOrderId(Long orderId);
}
