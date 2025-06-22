package com.aditya2254.ecommerceapp.ordersservice.exceptions;

public class OrderFailedException extends RuntimeException {
    public OrderFailedException(Exception ex) {
        super(ex.getMessage());
    }
}
