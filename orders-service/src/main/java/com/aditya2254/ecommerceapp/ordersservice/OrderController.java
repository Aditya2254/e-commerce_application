package com.aditya2254.ecommerceapp.ordersservice;

import com.aditya2254.ecommerceapp.ordersservice.dataaccess.CustomResponse;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderItemRequest;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderRequest;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderResponse;
import com.aditya2254.ecommerceapp.ordersservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.ordersservice.dto.ProductDTO;
import com.aditya2254.ecommerceapp.ordersservice.entity.CartItems;
import com.aditya2254.ecommerceapp.ordersservice.entity.Orders;
import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import com.aditya2254.ecommerceapp.ordersservice.services.OrderService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.server.ResponseStatusException;

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
    @Autowired
    private OrdersRepository ordersRepository;

    @PostMapping(path = "/cart/remove")
    public ResponseEntity<CustomResponse> removeFromCart(
            @RequestBody OrderItemRequest orderItemRequest,
            @RequestHeader("X-User-ID") String userId){
        try {
            orderService.removeFromCart(orderItemRequest, userId);
            return ResponseEntity.ok(new CustomResponse<>("Product removed from cart successfully"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(
                    toCustomResponse(e.getReason())
            );
        } catch (FeignException e) {
            String responseBody = e.contentUTF8();
            int status = e.status();
            return ResponseEntity.status(status).body(
                    toCustomResponse((responseBody != null && !responseBody.isBlank())
                            ? responseBody
                            : "Downstream service error")
            );
        }
    }

@PostMapping(path = "/cart/add")
public ResponseEntity<CustomResponse> addToCart(
        @RequestBody OrderItemRequest orderItemRequest,
        @RequestHeader("X-User-ID") String userId) {
    try {
        var result = orderService.addToCart(orderItemRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CustomResponse<ProductDTO>("Product added to cart successfully", result));
    } catch (ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(
                toCustomResponse(e.getReason())
        );
    } catch (FeignException e) {
        String responseBody = e.contentUTF8();
        int status = e.status();
        return ResponseEntity.status(status).body(
                toCustomResponse((responseBody != null && !responseBody.isBlank())
                        ? responseBody
                        : "Downstream service error")
        );
    }
}

@GetMapping(path = "/cart")
public ResponseEntity<List<CartItems>> getCartItems(@RequestHeader("X-User-ID") String userId) {
    try {
        List<CartItems> cartItems = orderService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cartItems);
        }
        return ResponseEntity.ok(cartItems);
    }catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    // OrderController.java
    @PostMapping(path = "/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request,
            @RequestHeader("X-User-ID") String userId) {

        // 1. Verify products exist and get prices
            List<OrderItems> orderItems;
            List<OrderItemRequest> lstRequestItems = new java.util.ArrayList<>();
                    try{
                        if(request.getItems().isEmpty()){   //if the items are empty, take it from cart
                            List<CartItems> cartItems = orderService.getCartItems(userId);
                            if(cartItems.isEmpty()){
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(toResponse(null, "failed", "Cart is empty"));
                            }
                            orderItems = cartItems.stream()
                                    .map(item -> {
                                        lstRequestItems.add(new OrderItemRequest(item.getProductId(), item.getQuantity()));
                                        ProductDTO product = productClient.getProduct(item.getProductId());
                                        return new OrderItems(product.id(), item.getQuantity(), product.price());
                                    }).toList();
                        }else {     // if they are not empty, take it from the request
                            orderItems = request.getItems().stream()
                            .map(item -> {
                                lstRequestItems.add(item);
                                ProductDTO product = productClient.getProduct(item.getProductId());
                                return new OrderItems(product.id(), item.getQuantity(), product.price());
                            }).toList();

                        }
                    }catch (FeignException e){
                        return ResponseEntity.status(e.status()).body(toResponse(null,
                                "failed",
                                "Error: Product not found for id: %s".formatted(lstRequestItems.isEmpty() ? "null" : lstRequestItems.get(lstRequestItems.size() - 1).getProductId().toString())));
                    }catch (ResponseStatusException e){
                        return ResponseEntity.status(e.getStatusCode()).body(toResponse(null, "failed", e.getReason()));
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(toResponse(null, "failed", "An unexpected error occurred: " + e.getMessage()));
                    }

        // 2. Reserve inventory
        InventoryReservationRequest reservationRequest = new InventoryReservationRequest(
                orderItems.stream().collect(Collectors.toMap( OrderItems::getProductId, OrderItems::getQuantity))
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
        Orders orders = orderService.createOrder(userId, orderItems, request.getShippingAddress(), request.getPaymentMethodId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(orders, "success","Order created successfully"));
    }

    @PutMapping(path = "/orders/modify")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestBody Orders orders,
            @RequestHeader("X-User-ID") String userId) throws Exception {

        // Check if the order exists
        Orders existingOrder = ordersRepository.findById(orders.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        // Check if the user ID matches
        if (!existingOrder.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(toResponse(null, "failed", "You are not authorized to modify this order"));
        }
        // Check if the order is already cancelled or completed
        if (!"Active".equalsIgnoreCase(existingOrder.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(toResponse(existingOrder, "failed", "Order is already "+ existingOrder.getStatus()));
        }

        try{
            Orders modifiedOrder =  orderService.modifyOrder(orders);
            return ResponseEntity.status(HttpStatus .OK).body(toResponse(modifiedOrder, "success", "Order modified successfully"));
        }catch (ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode()).body(toResponse(null, "failed", e.getReason()));
        }

    }

    @GetMapping(path = "/orders")
    public ResponseEntity<List<Orders>> getOrders() {
        return ResponseEntity.ok(ordersRepository.findAll());
    }

    @GetMapping(path = "/orders/{id}")
    public ResponseEntity<Orders> getOrderById(@PathVariable("id") Long orderId) {
        return ordersRepository.findById(orderId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //create a get mapping for getting orderItems by orderId
    @GetMapping(path = "/orderItems/{id}")
    public ResponseEntity<List<OrderItems>> getOrderItemsById(@PathVariable("id") Long orderId) {
        List<OrderItems> orderItems = orderService.getOrderItemsByOrderId(orderId);
        if (orderItems.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderItems);
    }

    private OrderResponse toResponse(Orders orders, String status, String message) {
        OrderResponse resp = new OrderResponse();
        resp.setOrderId((orders!=null)?orders.getOrderId():null);
        // set other fields as necessary
        resp.setStatus(status);
        resp.setMessage(message);
        return resp;
    }

    private CustomResponse<String> toCustomResponse(String message) {
        CustomResponse<String> response = new CustomResponse<>();
        response.setMessage(message);
        return response;
    }


}