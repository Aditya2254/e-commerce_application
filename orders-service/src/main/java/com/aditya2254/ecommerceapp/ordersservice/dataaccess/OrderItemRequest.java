package com.aditya2254.ecommerceapp.ordersservice.dataaccess;

public class OrderItemRequest {
    private Long productId;
    private Integer quantity;

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
