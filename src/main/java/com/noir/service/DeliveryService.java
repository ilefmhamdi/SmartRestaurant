package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.exception.AppException;
import com.noir.model.Order;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class DeliveryService {

    private static final Set<String> DELIVERY_VISIBLE_STATUSES = Set.of("ready", "out_for_delivery");

    private final JsonRepository<Order> orderRepo;

    public DeliveryService(AppConfig cfg) {
        this.orderRepo = new JsonRepository<>(cfg.getDataDir() + "/orders.json", Order.class);
    }

    // ---------------------------------------------------------------
    // Orders — delivery sees ready + out_for_delivery
    // ---------------------------------------------------------------
    public List<Order> listDeliveryOrders() {
        return orderRepo.read().stream()
                .filter(o -> DELIVERY_VISIBLE_STATUSES.contains(o.getStatus()))
                .toList();
    }

    public Order getDeliveryOrder(String orderNumber) {
        Order order = orderRepo.read().stream()
                .filter(o -> o.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst()
                .orElseThrow(() -> new AppException("Order " + orderNumber + " not found", 404));
        if (!DELIVERY_VISIBLE_STATUSES.contains(order.getStatus())) {
            throw new AppException("Order is not ready for delivery", 403);
        }
        return order;
    }

    // ---------------------------------------------------------------
    // Pickup — transitions ready → out_for_delivery
    // ---------------------------------------------------------------
    public Order pickup(String orderNumber, String actorUsername) {
        List<Order> orders = orderRepo.read();
        int idx = findOrderIdx(orders, orderNumber);
        Order order = orders.get(idx);

        if (!"ready".equals(order.getStatus())) {
            throw new AppException(
                "Order must be 'ready' before pickup. Current status: " + order.getStatus(), 400);
        }

        order.setStatus("out_for_delivery");
        order.setDeliveryStaffId(actorUsername);
        order.setDeliveryStaffName(actorUsername);
        updateStepState(order, "out_for_delivery");
        orderRepo.write(orders);
        return order;
    }

    // ---------------------------------------------------------------
    // Delivered — transitions out_for_delivery → delivered
    // ---------------------------------------------------------------
    public Order markDelivered(String orderNumber, String actorUsername) {
        List<Order> orders = orderRepo.read();
        int idx = findOrderIdx(orders, orderNumber);
        Order order = orders.get(idx);

        if (!"out_for_delivery".equals(order.getStatus())) {
            throw new AppException(
                "Order must be 'out_for_delivery' before marking delivered. Current status: " + order.getStatus(), 400);
        }

        order.setStatus("delivered");
        order.setDeliveryStaffId(actorUsername);
        order.setDeliveryStaffName(actorUsername);
        updateStepState(order, "delivered");
        orderRepo.write(orders);
        return order;
    }

    public Order markUndelivered(String orderNumber, String reason, String actorUsername) {
        List<Order> orders = orderRepo.read();
        int idx = findOrderIdx(orders, orderNumber);
        Order order = orders.get(idx);

        if (!"out_for_delivery".equals(order.getStatus())) {
            throw new AppException(
                "Order must be 'out_for_delivery' before marking undelivered. Current status: " + order.getStatus(), 400);
        }

        order.setStatus("undelivered");
        order.setUndeliveredReason(reason);
        order.setDeliveryStaffId(actorUsername);
        order.setDeliveryStaffName(actorUsername);
        updateStepState(order, "undelivered");
        orderRepo.write(orders);
        return order;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private int findOrderIdx(List<Order> orders, String orderNumber) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderNumber().equalsIgnoreCase(orderNumber)) return i;
        }
        throw new AppException("Order " + orderNumber + " not found", 404);
    }

    private void updateStepState(Order order, String newStatus) {
        if (order.getSteps() == null) return;
        for (Order.OrderStep step : order.getSteps()) {
            switch (newStatus) {
                case "out_for_delivery" -> {
                    if ("Out for Delivery".equals(step.getLabel())) step.setState("active");
                    if ("Being Prepared".equals(step.getLabel()))  step.setState("done");
                }
                case "delivered" -> {
                    if ("Delivered".equals(step.getLabel()))        step.setState("done");
                    if ("Out for Delivery".equals(step.getLabel())) step.setState("done");
                }
            }
        }
    }
}
