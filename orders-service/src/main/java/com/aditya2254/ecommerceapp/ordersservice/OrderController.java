package com.aditya2254.ecommerceapp.ordersservice;

import com.aditya2254.ecommerceapp.ordersservice.dataaccess.CustomResponse;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderItemRequest;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderRequest;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderResponse;
import com.aditya2254.ecommerceapp.ordersservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.ordersservice.dto.ProductDTO;
import com.aditya2254.ecommerceapp.ordersservice.entity.Orders;
import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import com.aditya2254.ecommerceapp.ordersservice.exceptions.OrderFailedException;
import com.aditya2254.ecommerceapp.ordersservice.services.OrderService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    @Autowired
    ProductClient productClient;

    @Autowired
    OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    // OrderController.java
    @PostMapping(path = "/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request,
            @RequestHeader("X-User-ID") String userId) {

        // 1. Verify products exist and get prices
            List<OrderItems> orderItems;
            List<OrderItemRequest> currentItem = new java.util.ArrayList<>();
                    try{
                        orderItems = request.getItems().stream()
                        .map(item -> {
                            currentItem.add(item);
                            ProductDTO product = productClient.getProduct(item.getProductId());
                            return new OrderItems(product.id(), item.getQuantity(), product.price());
                        }).toList();
                    }catch (FeignException e){
                        String responseBody = e.contentUTF8();
                        return ResponseEntity.status(e.status()).body(toResponse(null,
                                "failed",
                                "Error: Product not found for id: %s".formatted(currentItem.isEmpty() ? "null" : currentItem.get(currentItem.size() - 1).getProductId().toString())));
                    }

        // 2. Reserve inventory
        InventoryReservationRequest reservationRequest = new InventoryReservationRequest(
                request.getItems().stream()
                        .collect(Collectors.toMap(OrderItemRequest::getProductId, OrderItemRequest::getQuantity))
        );
        try {
            CustomResponse responseEntity = productClient.reserveInventory(reservationRequest);
        } catch (FeignException e) {
            String responseBody = e.contentUTF8();
            CustomResponse<String> customResponse;
            try {
                customResponse = objectMapper.readValue(responseBody, new TypeReference<CustomResponse<String>>() {
                });
            } catch (JsonProcessingException ex) {
                customResponse = new CustomResponse<>(responseBody.isEmpty() ? e.getMessage() : responseBody);
            }
            return ResponseEntity.status(e.status()).body(toResponse(null, "failed", customResponse.getMessage()));
        }

        // 3. Create orders
        Orders orders = orderService.createOrder(userId, orderItems);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(orders, "success","Order created successfully"));
    }

    private OrderResponse toResponse(Orders orders, String status, String message) {
        OrderResponse resp = new OrderResponse();
        resp.setOrderId((orders!=null)?orders.getOrderId():null);
        // set other fields as necessary
        resp.setStatus(status);
        resp.setMessage(message);
        return resp;
    }


}