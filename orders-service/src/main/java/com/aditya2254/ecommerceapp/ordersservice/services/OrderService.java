package com.aditya2254.ecommerceapp.ordersservice.services;

import com.aditya2254.ecommerceapp.ordersservice.entity.Orders;
import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import com.aditya2254.ecommerceapp.ordersservice.repositories.OrderItemsRepository;
import com.aditya2254.ecommerceapp.ordersservice.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemsRepository orderItemsRepository;


    public Orders createOrder(String userId, List<OrderItems> orderItems) {
        double total = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setTotal(total);
        Orders savedOrders = orderRepository.save(orders);

        orderItems.forEach(item -> item.setOrderId(savedOrders.getOrderId()));
        orderItemsRepository.saveAll(orderItems);

        return savedOrders;
    }
}
