package com.aditya2254.ecommerceapp.ordersservice.dataaccess;

public class OrderResponse {
    private Long orderId;
    private String status;
    private String message;
    // other necessary fields, plus getters/setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
