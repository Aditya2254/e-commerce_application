package com.aditya2254.ecommerceapp.ordersservice;

import com.aditya2254.ecommerceapp.ordersservice.dataaccess.CustomResponse;
import com.aditya2254.ecommerceapp.ordersservice.dto.InventoryReservationRequest;
import com.aditya2254.ecommerceapp.ordersservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDTO getProduct(@PathVariable("id") Long productId);

    @PostMapping("/products/reserve")
    CustomResponse reserveInventory(@RequestBody InventoryReservationRequest request);

}
