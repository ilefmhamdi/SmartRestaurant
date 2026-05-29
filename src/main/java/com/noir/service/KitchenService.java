package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.OrderStatusUpdateRequest;
import com.noir.dto.request.SpecialPlateRequest;
import com.noir.exception.AppException;
import com.noir.model.*;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class KitchenService {

    // Statuses kitchen staff are allowed to assign
    private static final Set<String> KITCHEN_ALLOWED_STATUSES = Set.of("being_prepared", "ready");
    // Statuses visible to kitchen
    private static final Set<String> KITCHEN_VISIBLE_STATUSES = Set.of("confirmed", "being_prepared", "ready");

    private final JsonRepository<Order> orderRepo;
    private final JsonRepository<Reservation> reservationRepo;
    private final JsonRepository<SpecialPlate> specialPlateRepo;
    private final JsonRepository<MenuItem> menuRepo;

    public KitchenService(AppConfig cfg) {
        String dir = cfg.getDataDir();
        this.orderRepo       = new JsonRepository<>(dir + "/orders.json",         Order.class);
        this.reservationRepo = new JsonRepository<>(dir + "/reservations.json",   Reservation.class);
        this.specialPlateRepo= new JsonRepository<>(dir + "/special_plates.json", SpecialPlate.class);
        this.menuRepo        = new JsonRepository<>(dir + "/menu.json",            MenuItem.class);
    }

    // ---------------------------------------------------------------
    // Orders — kitchen sees confirmed/being_prepared/ready
    // ---------------------------------------------------------------
    public List<Order> listKitchenOrders() {
        return orderRepo.read().stream()
                .filter(o -> KITCHEN_VISIBLE_STATUSES.contains(o.getStatus()))
                .toList();
    }

    public Order getKitchenOrder(String orderNumber) {
        Order order = orderRepo.read().stream()
                .filter(o -> o.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst()
                .orElseThrow(() -> new AppException("Order " + orderNumber + " not found", 404));
        if (!KITCHEN_VISIBLE_STATUSES.contains(order.getStatus())) {
            throw new AppException("Order is not in a kitchen-visible state", 403);
        }
        return order;
    }

    public Order updateOrderStatus(String orderNumber, OrderStatusUpdateRequest req, String actorUsername) {
        String newStatus = req.getStatus();
        if (!KITCHEN_ALLOWED_STATUSES.contains(newStatus)) {
            throw new AppException(
                "Kitchen can only set status to 'being_prepared' or 'ready'. Got: " + newStatus, 400);
        }

        List<Order> orders = orderRepo.read();
        int idx = findOrderIdx(orders, orderNumber);
        Order order = orders.get(idx);

        // Validate transition
        validateKitchenTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setKitchenStaffId(actorUsername);
        order.setKitchenStaffName(actorUsername);
        if (req.getKitchenNotes() != null && !req.getKitchenNotes().isBlank()) {
            order.setKitchenNotes(req.getKitchenNotes());
        }

        // Update the matching step state
        updateStepState(order, newStatus);

        orderRepo.write(orders);
        return order;
    }

    public Order rejectOrder(String orderNumber, String reason, String actorUsername) {
        List<Order> orders = orderRepo.read();
        int idx = findOrderIdx(orders, orderNumber);
        Order order = orders.get(idx);
        if (order.getStatus() == null || "delivered".equals(order.getStatus()) || "out_for_delivery".equals(order.getStatus())) {
            throw new AppException("Cannot reject an order that is already out for delivery or delivered", 400);
        }

        order.setStatus("rejected");
        order.setRejectedReason(reason);
        order.setKitchenStaffId(actorUsername);
        order.setKitchenStaffName(actorUsername);
        updateStepState(order, "rejected");

        orderRepo.write(orders);
        return order;
    }

    public Map<String, Object> rejectReservation(String reservationId, String reason, String actorUsername) {
        List<Reservation> reservations = reservationRepo.read();
        int idx = -1;
        for (int i = 0; i < reservations.size(); i++) {
            if (reservationId.equals(reservations.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Reservation not found", 404);

        Reservation reservation = reservations.get(idx);
        if ("delivered".equals(reservation.getStatus()) || "rejected".equals(reservation.getStatus())) {
            throw new AppException("Cannot reject this reservation", 400);
        }
        reservation.setStatus("rejected");
        reservation.setRejectedReason(reason);
        reservation.setNotes((reservation.getNotes() != null ? reservation.getNotes() + "\n" : "")
                + "Rejected by " + actorUsername + ": " + reason);
        reservations.set(idx, reservation);
        reservationRepo.write(reservations);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reservation", reservation);
        result.put("rejectedBy", actorUsername);
        result.put("rejectedAt", Instant.now().toString());
        return result;
    }

    // ---------------------------------------------------------------
    // Reservations — kitchen sees today's + upcoming with allergy info
    // ---------------------------------------------------------------
    public List<Map<String, Object>> listKitchenReservations() {
        String today = LocalDate.now().toString(); // YYYY-MM-DD
        return reservationRepo.read().stream()
                .filter(r -> r.getDate() != null && r.getDate().compareTo(today) >= 0)
                .map(this::toKitchenReservation)
                .toList();
    }

    public List<MenuItem> listMenu() {
        return menuRepo.read();
    }

    // ---------------------------------------------------------------
    // Special plates
    // ---------------------------------------------------------------
    public List<SpecialPlate> listSpecialPlates() {
        return specialPlateRepo.read();
    }

    public SpecialPlate createSpecialPlate(SpecialPlateRequest req, String createdBy) {
        List<SpecialPlate> plates = specialPlateRepo.read();
        String now = Instant.now().toString();

        SpecialPlate plate = new SpecialPlate();
        plate.setId(UUID.randomUUID().toString());
        plate.setRecommendedBy(createdBy);
        plate.setCreatedAt(now);
        plate.setUpdatedAt(now);
        applyPlateRequest(plate, req);

        plates.add(0, plate);
        specialPlateRepo.write(plates);
        return plate;
    }

    public SpecialPlate updateSpecialPlate(String id, SpecialPlateRequest req, String updatedBy) {
        List<SpecialPlate> plates = specialPlateRepo.read();
        int idx = -1;
        for (int i = 0; i < plates.size(); i++) {
            if (id.equals(plates.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Special plate not found", 404);

        SpecialPlate plate = plates.get(idx);
        plate.setUpdatedAt(Instant.now().toString());
        applyPlateRequest(plate, req);

        specialPlateRepo.write(plates);
        return plate;
    }

    public Map<String, Boolean> deleteSpecialPlate(String id) {
        List<SpecialPlate> plates = specialPlateRepo.read();
        boolean removed = plates.removeIf(p -> id.equals(p.getId()));
        if (!removed) throw new AppException("Special plate not found", 404);
        specialPlateRepo.write(plates);
        return Map.of("deleted", true);
    }

    // ---------------------------------------------------------------
    // Chef recommendation — marks an existing menu item as recommended
    // ---------------------------------------------------------------
    public Map<String, Object> recommendMenuItem(int menuItemId, String recommendedBy) {
        List<MenuItem> items = menuRepo.read();
        MenuItem item = items.stream()
                .filter(i -> i.getId() != null && i.getId() == menuItemId)
                .findFirst()
                .orElseThrow(() -> new AppException("Menu item " + menuItemId + " not found", 404));

        // Add "recommended" badge if not already present
        List<String> badges = new ArrayList<>(item.getBadges() != null ? item.getBadges() : List.of());
        if (!badges.contains("recommended")) {
            badges.add("recommended");
            item.setBadges(badges);
            menuRepo.write(items);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("menuItem", item);
        result.put("recommendedBy", recommendedBy);
        result.put("recommendedAt", Instant.now().toString());
        return result;
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void applyPlateRequest(SpecialPlate plate, SpecialPlateRequest req) {
        plate.setName(req.getName().trim());
        plate.setCat(req.getCat());
        plate.setImage(req.getImage().trim());
        plate.setPrice(req.getPrice());
        plate.setDesc(req.getDesc().trim());
        plate.setAllergens(req.getAllergens() != null ? req.getAllergens() : List.of());
        plate.setRating(req.getRating());
        plate.setReviews(req.getReviews());
        plate.setBadges(req.getBadges() != null ? req.getBadges() : List.of());
        plate.setVeg(req.getVeg());
        plate.setAvailableFrom(req.getAvailableFrom());
        plate.setAvailableTo(req.getAvailableTo());
        plate.setAvailableDays(req.getAvailableDays());
        plate.setRecommended(Boolean.TRUE.equals(req.getRecommended()));
    }

    private void validateKitchenTransition(String current, String next) {
        boolean valid = switch (next) {
            case "being_prepared" -> "confirmed".equals(current);
            case "ready"          -> "being_prepared".equals(current);
            default -> false;
        };
        if (!valid) {
            throw new AppException(
                "Cannot transition from '" + current + "' to '" + next + "'", 400);
        }
    }

    private void updateStepState(Order order, String newStatus) {
        if (order.getSteps() == null) return;
        for (Order.OrderStep step : order.getSteps()) {
            switch (newStatus) {
                case "being_prepared" -> {
                    if ("Being Prepared".equals(step.getLabel())) step.setState("active");
                    if ("Order Confirmed".equals(step.getLabel())) step.setState("done");
                }
                case "ready" -> {
                    if ("Being Prepared".equals(step.getLabel())) step.setState("done");
                    if ("Out for Delivery".equals(step.getLabel())) step.setState("active");
                }
            }
        }
    }

    private int findOrderIdx(List<Order> orders, String orderNumber) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderNumber().equalsIgnoreCase(orderNumber)) return i;
        }
        throw new AppException("Order " + orderNumber + " not found", 404);
    }

    private Map<String, Object> toKitchenReservation(Reservation r) {
        // Kitchen gets full allergy/menu selection info but no raw email/phone
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               r.getId());
        m.put("confirmationCode", r.getConfirmationCode());
        m.put("status",           r.getStatus());
        m.put("date",             r.getDate());
        m.put("time",             r.getTime());
        m.put("guests",           r.getGuests());
        m.put("experience",       r.getExperience());
        m.put("firstName",        r.getFirstName());
        m.put("lastName",         r.getLastName());
        m.put("allergySelections",r.getAllergySelections());
        m.put("menuSelections",   r.getMenuSelections());
        m.put("specialRequests",  r.getSpecialRequests());
        m.put("notes",            r.getNotes());
        return m;
    }
}
