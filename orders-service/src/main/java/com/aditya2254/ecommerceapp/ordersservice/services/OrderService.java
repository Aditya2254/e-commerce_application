package com.aditya2254.ecommerceapp.ordersservice.services;

import com.aditya2254.ecommerceapp.ordersservice.ProductClient;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.CustomResponse;
import com.aditya2254.ecommerceapp.ordersservice.dataaccess.OrderItemRequest;
import com.aditya2254.ecommerceapp.ordersservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.ordersservice.dto.ProductDTO;
import com.aditya2254.ecommerceapp.ordersservice.entity.CartItems;
import com.aditya2254.ecommerceapp.ordersservice.entity.Orders;
import com.aditya2254.ecommerceapp.ordersservice.entity.OrderItems;
import com.aditya2254.ecommerceapp.ordersservice.repositories.CartItemsRepository;
import com.aditya2254.ecommerceapp.ordersservice.repositories.OrderItemsRepository;
import com.aditya2254.ecommerceapp.ordersservice.repositories.OrderRepository;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private CartItemsRepository cartItemsRepository;

    public ProductDTO addToCart(
            OrderItemRequest orderItem,
            String userId) {
        ProductDTO product;
        try{
            product = productClient.getProduct(orderItem.getProductId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get product details");
        }
        /*     already doing this at the time of order creation
        if (product.stock() < orderItem.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.name());
        }*/
        // Check if the product is already in the cart
        List<CartItems> existingItems = cartItemsRepository.findByUserIdAndProductId(userId, orderItem.getProductId());
        if(existingItems.isEmpty()) {
            CartItems cartItem = new CartItems();
            cartItem.setUserId(userId);
            cartItem.setProductId(orderItem.getProductId());
            cartItem.setQuantity(orderItem.getQuantity());
            cartItem.setPrice(product.price());
            cartItemsRepository.saveAndFlush(cartItem);
        } else {
            // If the product is already in the cart, update the quantity
            CartItems existingCartItem = existingItems.get(0);
            existingCartItem.setQuantity(existingCartItem.getQuantity() + orderItem.getQuantity());
            cartItemsRepository.saveAndFlush(existingCartItem);
        }
        return product;
    }


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
        cartItemsRepository.deleteByUserId(userId);

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

    public List<CartItems> getCartItems(String userId) {
        return cartItemsRepository.findByUserId(userId);
    }

    public List<OrderItems> getOrderItemsByOrderId(Long orderId) {
        return orderItemsRepository.findByOrderId(orderId);
    }

    public void removeFromCart(OrderItemRequest orderItemRequest, String userId) {
        List<CartItems> existingItems = cartItemsRepository.findByUserIdAndProductId(userId, orderItemRequest.getProductId());
        if (existingItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in cart");
        }
        CartItems cartItem = existingItems.get(0);
        if (cartItem.getQuantity() <= orderItemRequest.getQuantity()) {
            cartItemsRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() - orderItemRequest.getQuantity());
            cartItemsRepository.save(cartItem);
        }
    }
}
