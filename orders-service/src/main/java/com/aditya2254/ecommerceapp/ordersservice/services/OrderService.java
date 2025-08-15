package com.aditya2254.ecommerceapp.ordersservice.services;

import com.aditya2254.ecommerceapp.ordersservice.ProductClient;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.CustomResponse;
import com.aditya2254.ecommerceapp.ordersservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.ordersservice.entity.Orders;
import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import com.aditya2254.ecommerceapp.ordersservice.repositories.OrderItemsRepository;
import com.aditya2254.ecommerceapp.ordersservice.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderService {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemsRepository orderItemsRepository;
    @Autowired
    private ProductClient productClient;


    public Orders createOrder(String userId, List<OrderItems> orderItems, String shippingAddress, String paymentMethodId) {
        double total = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setTotal(total);
        orders.setStatus("ACTIVE");
        orders.setShippingAddress(shippingAddress);
        orders.setPaymentMethodId(paymentMethodId);
        Orders savedOrders = orderRepository.save(orders);

        orderItems.forEach(item -> item.setOrderId(savedOrders.getOrderId()));
        orderItemsRepository.saveAll(orderItems);

        return savedOrders;
    }


    public Orders modifyOrder(Orders orders) throws Exception {
        Orders existingOrder = orderRepository.findById(orders.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        // Update the shipping address if provided
        if (orders.getShippingAddress() != null) {
            existingOrder.setShippingAddress(orders.getShippingAddress());
        }
        if(orders.getStatus()!= null && !orders.getStatus().isEmpty() && "Cancelled".equals(orders.getStatus())) {
            existingOrder.setStatus("Cancelled");

            //List<OrderItems> orderItems =  orderItemsRepository.findByOrderId(existingOrder.getOrderId());
            InventoryReservationRequest reservationRequest = new InventoryReservationRequest(
                    orderItemsRepository.findByOrderId(existingOrder.getOrderId()).stream()
                            .collect(Collectors.toMap(OrderItems::getProductId, OrderItems::getQuantity))
            );
            try{
            // Rollback inventory for the items in the order
            CustomResponse response = productClient.rollbackInventory(reservationRequest);
            }catch (Exception e){
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to rollback inventory";
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
            }
            orderItemsRepository.deleteByOrderId(existingOrder.getOrderId());
        }
        if(orders.getStatus()!= null && !orders.getStatus().isEmpty() && "Completed".equals(orders.getStatus())) {
            existingOrder.setStatus("Completed");
        }

        orderRepository.save(existingOrder);
        return existingOrder;
    }
}
