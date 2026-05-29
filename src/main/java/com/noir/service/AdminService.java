package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.*;
import com.noir.exception.AppException;
import com.noir.model.*;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class AdminService {

    private final JsonRepository<MenuItem> menuRepo;
    private final JsonRepository<Reservation> reservationRepo;
    private final JsonRepository<Order> orderRepo;
    private final JsonRepository<Review> reviewRepo;
    private final JsonRepository<NewsletterSubscriber> newsletterRepo;
    private final JsonRepository<Promotion> promotionRepo;
    private final JsonRepository<SpecialPlate> specialPlateRepo;
    private final JsonRepository<Ingredient> ingredientRepo;
    private final JsonRepository<TableInfo> tableRepo;

    // We delegate staff operations to StaffAuthService to keep password logic there
    private final StaffAuthService staffAuthService;

    public AdminService(AppConfig cfg, StaffAuthService staffAuthService) {
        String dir = cfg.getDataDir();
        this.menuRepo          = new JsonRepository<>(dir + "/menu.json",           MenuItem.class);
        this.reservationRepo   = new JsonRepository<>(dir + "/reservations.json",   Reservation.class);
        this.orderRepo         = new JsonRepository<>(dir + "/orders.json",          Order.class);
        this.reviewRepo        = new JsonRepository<>(dir + "/reviews.json",         Review.class);
        this.newsletterRepo    = new JsonRepository<>(dir + "/newsletter.json",      NewsletterSubscriber.class);
        this.promotionRepo     = new JsonRepository<>(dir + "/promotions.json",      Promotion.class);
        this.specialPlateRepo  = new JsonRepository<>(dir + "/special_plates.json",  SpecialPlate.class);
        this.ingredientRepo    = new JsonRepository<>(dir + "/ingredients.json",     Ingredient.class);
        this.tableRepo         = new JsonRepository<>(dir + "/tables.json",          TableInfo.class);
        this.staffAuthService  = staffAuthService;
    }

    // ---------------------------------------------------------------
    // Overview
    // ---------------------------------------------------------------
    public Map<String, Object> getOverview() {
        var menu         = menuRepo.read();
        var reservations = reservationRepo.read();
        var orders       = orderRepo.read();
        var reviews      = reviewRepo.read();
        var newsletter   = newsletterRepo.read();
        var promotions   = promotionRepo.read();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("menuItems",    menu.size());
        stats.put("reservations", reservations.size());
        stats.put("orders",       orders.size());
        stats.put("reviews",      reviews.size());
        stats.put("subscribers",  newsletter.size());
        stats.put("promotions",   promotions.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats",               stats);
        result.put("recentReservations",  reservations.stream().limit(5).map(this::maskReservation).toList());
        result.put("recentOrders",        orders.stream().limit(5).toList());
        result.put("recentReviews",       reviews.stream().limit(5).toList());
        return result;
    }

    // ---------------------------------------------------------------
    // Menu
    // ---------------------------------------------------------------
    public List<MenuItem> listMenu() { return menuRepo.read(); }

    public MenuItem createMenuItem(MenuItemRequest req) {
        List<MenuItem> items = menuRepo.read();
        int nextId = items.stream().mapToInt(i -> i.getId() != null ? i.getId() : 0).max().orElse(0) + 1;
        MenuItem item = toMenuItem(nextId, req);
        items.add(item);
        menuRepo.write(items);
        return item;
    }

    public MenuItem updateMenuItem(int id, MenuItemRequest req) {
        List<MenuItem> items = menuRepo.read();
        int idx = findMenuIdx(items, id);
        MenuItem item = toMenuItem(id, req);
        items.set(idx, item);
        menuRepo.write(items);
        return item;
    }

    public List<Ingredient> listIngredients() {
        return ingredientRepo.read();
    }

    public Ingredient createIngredient(IngredientRequest req) {
        List<Ingredient> list = ingredientRepo.read();
        Ingredient ingredient = new Ingredient();
        ingredient.setId(UUID.randomUUID().toString());
        applyIngredient(ingredient, req);
        list.add(0, ingredient);
        ingredientRepo.write(list);
        return ingredient;
    }

    public Ingredient updateIngredient(String id, IngredientRequest req) {
        List<Ingredient> list = ingredientRepo.read();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Ingredient not found", 404);
        Ingredient ingredient = list.get(idx);
        applyIngredient(ingredient, req);
        ingredientRepo.write(list);
        return ingredient;
    }

    public Map<String, Boolean> deleteIngredient(String id) {
        List<Ingredient> list = ingredientRepo.read();
        boolean removed = list.removeIf(i -> id.equals(i.getId()));
        if (!removed) throw new AppException("Ingredient not found", 404);
        ingredientRepo.write(list);
        return Map.of("deleted", true);
    }

    public List<TableInfo> listTables() {
        return tableRepo.read();
    }

    public TableInfo createTable(TableRequest req) {
        List<TableInfo> list = tableRepo.read();
        TableInfo table = new TableInfo();
        table.setId(req.getId().trim());
        applyTable(table, req);
        list.add(0, table);
        tableRepo.write(list);
        return table;
    }

    public TableInfo updateTable(String id, TableRequest req) {
        List<TableInfo> list = tableRepo.read();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Table not found", 404);
        TableInfo table = list.get(idx);
        applyTable(table, req);
        tableRepo.write(list);
        return table;
    }

    public Map<String, Boolean> deleteTable(String id) {
        List<TableInfo> list = tableRepo.read();
        boolean removed = list.removeIf(t -> id.equals(t.getId()));
        if (!removed) throw new AppException("Table not found", 404);
        tableRepo.write(list);
        return Map.of("deleted", true);
    }

    private void applyIngredient(Ingredient ingredient, IngredientRequest req) {
        ingredient.setName(req.getName().trim());
        ingredient.setStock(req.getStock());
        ingredient.setUnit(req.getUnit().trim());
        ingredient.setAvailable(req.getAvailable());
        ingredient.setNotes(req.getNotes());
    }

    private void applyTable(TableInfo table, TableRequest req) {
        table.setSeats(req.getSeats());
        table.setAvailable(req.getAvailable());
        table.setLocation(req.getLocation());
        table.setNotes(req.getNotes());
    }

    public Map<String, Object> updateStaff(String id, StaffUpdateRequest req) {
        return staffAuthService.updateStaff(id, req);
    }

    public Map<String, Boolean> deleteMenuItem(int id) {
        List<MenuItem> items = menuRepo.read();
        boolean removed = items.removeIf(i -> i.getId() != null && i.getId() == id);
        if (!removed) throw new AppException("Menu item not found", 404);
        menuRepo.write(items);
        return Map.of("deleted", true);
    }

    // ---------------------------------------------------------------
    // Reservations
    // ---------------------------------------------------------------
    public List<Map<String, Object>> listReservations() {
        return reservationRepo.read().stream().map(this::maskReservation).toList();
    }

    public Reservation getReservation(String id) {
        return reservationRepo.read().stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException("Reservation not found", 404));
    }

    public Reservation updateReservation(String id, ReservationAdminUpdateRequest req) {
        List<Reservation> list = reservationRepo.read();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Reservation not found", 404);
        Reservation r = list.get(idx);

        if (req.getNotes() != null) {
            r.setNotes(req.getNotes());
        }

        if (req.getRejectedReason() != null) {
            r.setRejectedReason(req.getRejectedReason());
        }

        if (req.getTableId() != null) {
            assignTableToReservation(r, req.getTableId(), req.getTableSeats());
        }

        if (req.getStatus() != null) {
            String status = req.getStatus().trim().toLowerCase();
            if ("confirmed".equals(status)) {
                if (r.getTableId() == null) {
                    if (!tryAutoAssignTable(r)) {
                        r.setStatus("rejected");
                        r.setRejectedReason("No available table matches the reservation requirements");
                        reservationRepo.write(list);
                        return r;
                    }
                }
                r.setStatus("confirmed");
            } else if ("rejected".equals(status)) {
                r.setStatus("rejected");
                if (r.getRejectedReason() == null || r.getRejectedReason().isBlank()) {
                    r.setRejectedReason("Reservation rejected");
                }
            } else {
                r.setStatus(req.getStatus());
            }
        }

        reservationRepo.write(list);
        return r;
    }

    public Map<String, Boolean> deleteReservation(String id) {
        List<Reservation> list = reservationRepo.read();
        Reservation removedReservation = list.stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException("Reservation not found", 404));

        boolean removed = list.removeIf(r -> id.equals(r.getId()));
        if (!removed) {
            throw new AppException("Reservation not found", 404);
        }

        if (removedReservation.getTableId() != null) {
            List<TableInfo> tables = tableRepo.read();
            tables.stream()
                    .filter(t -> removedReservation.getTableId().equals(t.getId()))
                    .findFirst()
                    .ifPresent(table -> table.setAvailable(true));
            tableRepo.write(tables);
        }

        reservationRepo.write(list);
        return Map.of("deleted", true);
    }

    private void assignTableToReservation(Reservation reservation, String tableId, Integer tableSeats) {
        List<TableInfo> tables = tableRepo.read();
        TableInfo table = tables.stream()
                .filter(t -> tableId.equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException("Table not found", 404));

        if (!Boolean.TRUE.equals(table.getAvailable()) && !tableId.equals(reservation.getTableId())) {
            throw new AppException("Table is not available", 400);
        }
        if (reservation.getGuests() != null && table.getSeats() != null && table.getSeats() < reservation.getGuests()) {
            throw new AppException("Table does not have enough seats for this reservation", 400);
        }

        reservation.setTableId(table.getId());
        reservation.setTableSeats(tableSeats != null ? tableSeats : table.getSeats());
        reservation.setRejectedReason(null);
    }

    private boolean tryAutoAssignTable(Reservation reservation) {
        if (reservation.getGuests() == null) return false;
        List<TableInfo> tables = tableRepo.read();
        Optional<TableInfo> matching = tables.stream()
                .filter(t -> Boolean.TRUE.equals(t.getAvailable()))
                .filter(t -> t.getSeats() != null && t.getSeats() >= reservation.getGuests())
                .findFirst();
        if (matching.isEmpty()) {
            return false;
        }
        TableInfo table = matching.get();
        reservation.setTableId(table.getId());
        reservation.setTableSeats(table.getSeats());
        table.setAvailable(false);
        tableRepo.write(tables);
        return true;
    }

    // ---------------------------------------------------------------
    // Orders — admin has full access, can set any status
    // ---------------------------------------------------------------
    public List<Order> listOrders() { return orderRepo.read(); }

    public Order updateOrderStatus(String orderNumber, OrderStatusUpdateRequest req) {
        List<Order> orders = orderRepo.read();
        int idx = -1;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getOrderNumber().equalsIgnoreCase(orderNumber)) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Order not found", 404);

        Order order = orders.get(idx);
        order.setStatus(req.getStatus());
        if (req.getKitchenNotes() != null && !req.getKitchenNotes().isBlank()) {
            order.setKitchenNotes(req.getKitchenNotes());
        }
        orderRepo.write(orders);
        return order;
    }

    public Map<String, Boolean> deleteOrder(String orderNumber) {
        List<Order> orders = orderRepo.read();
        boolean removed = orders.removeIf(o -> orderNumber.equals(o.getOrderNumber()));
        if (!removed) throw new AppException("Order not found", 404);
        orderRepo.write(orders);
        return Map.of("deleted", true);
    }

    // ---------------------------------------------------------------
    // Reviews
    // ---------------------------------------------------------------
    public List<Review> listReviews() { return reviewRepo.read(); }

    public Map<String, Boolean> deleteReview(String id) {
        List<Review> reviews = reviewRepo.read();
        boolean removed = reviews.removeIf(r -> id.equals(r.getId()));
        if (!removed) throw new AppException("Review not found", 404);
        reviewRepo.write(reviews);
        return Map.of("deleted", true);
    }

    // ---------------------------------------------------------------
    // Subscribers
    // ---------------------------------------------------------------
    public List<Map<String, Object>> listSubscribers() {
        return newsletterRepo.read().stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           s.getId());
            m.put("email",        s.getEmail());
            m.put("emailMasked",  maskEmail(s.getEmail()));
            m.put("subscribedAt", s.getSubscribedAt());
            m.put("source",       s.getSource() != null ? s.getSource() : "newsletter");
            return m;
        }).toList();
    }

    public NewsletterSubscriber getSubscriber(String id) {
        return newsletterRepo.read().stream()
                .filter(s -> id.equals(s.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException("Subscriber not found", 404));
    }

    public Map<String, Boolean> deleteSubscriber(String id) {
        List<NewsletterSubscriber> subs = newsletterRepo.read();
        boolean removed = subs.removeIf(s -> id.equals(s.getId()));
        if (!removed) throw new AppException("Subscriber not found", 404);
        newsletterRepo.write(subs);
        return Map.of("deleted", true);
    }

    // ---------------------------------------------------------------
    // Promotions
    // ---------------------------------------------------------------
    public List<Promotion> listPromotions() { return promotionRepo.read(); }

    public Promotion createPromotion(PromotionRequest req) {
        List<Promotion> list = promotionRepo.read();
        String now = Instant.now().toString();
        Promotion p = new Promotion();
        p.setId(UUID.randomUUID().toString());
        applyPromotion(p, req);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        list.add(0, p);
        promotionRepo.write(list);
        return p;
    }

    public Promotion updatePromotion(String id, PromotionRequest req) {
        List<Promotion> list = promotionRepo.read();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).getId())) { idx = i; break; }
        }
        if (idx < 0) throw new AppException("Promotion not found", 404);
        Promotion p = list.get(idx);
        applyPromotion(p, req);
        p.setUpdatedAt(Instant.now().toString());
        promotionRepo.write(list);
        return p;
    }

    public Promotion updatePromotionStatus(String id, String status) {
        Promotion existing = listPromotions().stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException("Promotion not found", 404));
        PromotionRequest req = new PromotionRequest();
        req.setTitle(existing.getTitle());
        req.setCode(existing.getCode());
        req.setDiscount(existing.getDiscount());
        req.setDescription(existing.getDescription());
        req.setStatus(status);
        req.setStartsAt(existing.getStartsAt() != null ? existing.getStartsAt() : "");
        req.setEndsAt(existing.getEndsAt() != null ? existing.getEndsAt() : "");
        return updatePromotion(id, req);
    }

    // ---------------------------------------------------------------
    // Staff management — delegated to StaffAuthService
    // ---------------------------------------------------------------
    public List<Map<String, Object>> listStaff() {
        return staffAuthService.listStaff();
    }

    public Map<String, Object> createStaff(CreateStaffRequest req) {
        return staffAuthService.createStaff(req);
    }

    public Map<String, Boolean> deleteStaff(String id) {
        return staffAuthService.deleteStaff(id);
    }

    // ---------------------------------------------------------------
    // Special plates — admin has full control
    // ---------------------------------------------------------------
    public List<SpecialPlate> listSpecialPlates() {
        return specialPlateRepo.read();
    }

    public SpecialPlate createSpecialPlate(SpecialPlateRequest req) {
        List<SpecialPlate> plates = specialPlateRepo.read();
        String now = Instant.now().toString();
        SpecialPlate plate = new SpecialPlate();
        plate.setId(UUID.randomUUID().toString());
        plate.setRecommendedBy("admin");
        plate.setCreatedAt(now);
        plate.setUpdatedAt(now);
        applyPlateRequest(plate, req);
        plates.add(0, plate);
        specialPlateRepo.write(plates);
        return plate;
    }

    public SpecialPlate updateSpecialPlate(String id, SpecialPlateRequest req) {
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
    // Chef recommendation — admin can also mark items as recommended
    // ---------------------------------------------------------------
    public Map<String, Object> recommendMenuItem(int menuItemId, String recommendedBy) {
        List<MenuItem> items = menuRepo.read();
        MenuItem item = items.stream()
                .filter(i -> i.getId() != null && i.getId() == menuItemId)
                .findFirst()
                .orElseThrow(() -> new AppException("Menu item " + menuItemId + " not found", 404));

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
    // Private helpers
    // ---------------------------------------------------------------
    private void applyPromotion(Promotion p, PromotionRequest req) {
        p.setTitle(req.getTitle());
        p.setCode(req.getCode().trim().toUpperCase());
        p.setDiscount(req.getDiscount());
        p.setDescription(req.getDescription());
        p.setStatus(req.getStatus() != null ? req.getStatus() : "active");
        p.setStartsAt(req.getStartsAt() != null ? req.getStartsAt() : "");
        p.setEndsAt(req.getEndsAt() != null ? req.getEndsAt() : "");
    }

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

    private MenuItem toMenuItem(int id, MenuItemRequest req) {
        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName(req.getName().trim());
        item.setCat(req.getCat());
        item.setImage(req.getImage().trim());
        item.setPrice(req.getPrice());
        item.setDesc(req.getDesc().trim());
        item.setAllergens(req.getAllergens());
        item.setRating(req.getRating());
        item.setReviews(req.getReviews());
        item.setBadges(req.getBadges());
        item.setVeg(req.getVeg());
        return item;
    }

    private int findMenuIdx(List<MenuItem> items, int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() != null && items.get(i).getId() == id) return i;
        }
        throw new AppException("Menu item not found", 404);
    }

    private Map<String, Object> maskReservation(Reservation r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               r.getId());
        m.put("clientId",         r.getClientId());
        m.put("confirmationCode", r.getConfirmationCode());
        m.put("status",           r.getStatus());
        m.put("createdAt",        r.getCreatedAt());
        m.put("firstName",        r.getFirstName());
        m.put("lastName",         r.getLastName());
        m.put("emailMasked",      maskEmail(r.getEmail()));
        m.put("phoneMasked",      maskPhone(r.getPhone()));
        m.put("date",             r.getDate());
        m.put("guests",           r.getGuests());
        m.put("time",             r.getTime());
        m.put("experience",       r.getExperience());
        return m;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String visible = local.length() > 2 ? local.substring(0, 2) : local.substring(0, 1);
        return visible + "***@" + parts[1];
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return phone.substring(0, phone.length() - 4).replaceAll("\\d", "*")
               + phone.substring(phone.length() - 4);
    }
}
