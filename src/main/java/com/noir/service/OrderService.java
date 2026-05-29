package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.exception.AppException;
import com.noir.model.Order;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final JsonRepository<Order> repo;

    public OrderService(AppConfig appConfig) {
        this.repo = new JsonRepository<>(appConfig.getDataDir() + "/orders.json", Order.class);
    }

    public Order track(String orderNumber) {
        return repo.read().stream()
                .filter(o -> o.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst()
                .orElseThrow(() -> new AppException("Order " + orderNumber + " not found", 404));
    }

    public JsonRepository<Order> getRepo() { return repo; }
}