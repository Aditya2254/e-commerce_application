package com.aditya2254.ecommerceapp.ordersservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String notEnoughStockAvailable) {
        super(notEnoughStockAvailable);
    }
}
