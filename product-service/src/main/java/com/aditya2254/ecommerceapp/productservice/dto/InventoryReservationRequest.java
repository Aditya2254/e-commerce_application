package com.aditya2254.ecommerceapp.productservice.dto;

import java.util.Map;

public record InventoryReservationRequest(Map<Long, Integer> items) {

    public InventoryReservationRequest(Map<Long, Integer> items) {
        this.items = items;
    }

    @Override
    public Map<Long, Integer> items() {
        return items;
    }
}
