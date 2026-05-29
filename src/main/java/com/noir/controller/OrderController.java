package com.noir.controller;

import com.noir.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<?> track(@PathVariable String orderNumber) {
        return ResponseEntity.ok(Map.of("success", true, "data", orderService.track(orderNumber)));
    }
}