package com.aditya2254.ecommerceapp.ordersservice.dto;

public record ProductDTO(Long id, String name, Double price, Integer stock) {

    public ProductDTO(Long id, String name, Double price, Integer stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Double price() {
        return price;
    }

    @Override
    public Integer stock() {
        return stock;
    }
}
