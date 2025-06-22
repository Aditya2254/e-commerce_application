package com.aditya2254.ecommerceapp.ordersservice.dataaccess;

public class CustomResponse<T> {
    private String message;
    private T data;

    public CustomResponse() {
    }

    public CustomResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public CustomResponse(String message) {
        this.message = message;
        this.data = null;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}