package com.aditya2254.ecommerceapp.ordersservice.dataaccess;

import java.util.List;

public class OrderRequest {
    private List<OrderItemRequest> items;
    private String shippingAddress;
    private String paymentMethodId;

    // Getters and setters
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
}
