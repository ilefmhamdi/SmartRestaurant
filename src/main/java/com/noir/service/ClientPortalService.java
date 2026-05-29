package com.noir.service;

import com.noir.config.AppConfig;
import com.noir.dto.request.ClientOrderRequest;
import com.noir.dto.request.ClientReservationRequest;
import com.noir.dto.request.ClientReviewRequest;
import com.noir.exception.AppException;
import com.noir.model.*;
import com.noir.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Year;
import java.util.*;

@Service
public class ClientPortalService {

    private final JsonRepository<MenuItem> menuRepo;
    private final JsonRepository<Reservation> reservationRepo;
    private final JsonRepository<Order> orderRepo;
    private final JsonRepository<Review> reviewRepo;
    private final JsonRepository<Promotion> promotionRepo;
    private final JsonRepository<TableInfo> tableRepo;

    public ClientPortalService(AppConfig cfg) {
        String dir = cfg.getDataDir();
        this.menuRepo        = new JsonRepository<>(dir + "/menu.json",         MenuItem.class);
        this.reservationRepo = new JsonRepository<>(dir + "/reservations.json", Reservation.class);
        this.orderRepo       = new JsonRepository<>(dir + "/orders.json",        Order.class);
        this.reviewRepo      = new JsonRepository<>(dir + "/reviews.json",       Review.class);
        this.promotionRepo   = new JsonRepository<>(dir + "/promotions.json",    Promotion.class);
        this.tableRepo       = new JsonRepository<>(dir + "/tables.json",        TableInfo.class);
    }

    public Map<String, Object> getPortal(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user",       user);
        result.put("navigation", List.of("Menu", "Reserve", "Track Order", "Loyalty", "Offers", "Chef", "About us"));
        result.put("menu",       listMenu());
        result.put("loyalty",    getLoyalty(user));
        result.put("offers",     getOffers());
        result.put("chef",       getChef());
        result.put("about",      getAbout());
        return result;
    }

    public List<MenuItem> listMenu() { return menuRepo.read(); }

    public Reservation createReservation(User user, ClientReservationRequest req) {
        List<Reservation> list = reservationRepo.read();
        String code = "NOIR-" + Year.now().getValue() + "-" + String.format("%04d", list.size() + 1);

        Reservation r = new Reservation();
        r.setId(UUID.randomUUID().toString());
        r.setClientId(user.getId());
        r.setConfirmationCode(code);
        r.setStatus("confirmed");
        r.setCreatedAt(Instant.now().toString());
        r.setFirstName(user.getFirstName());
        r.setLastName(user.getLastName());
        r.setEmail(user.getEmail());
        r.setPhone(req.getPhone());
        r.setDate(req.getDate());
        r.setGuests(req.getGuests());
        r.setTime(req.getTime());
        r.setExperience(req.getExperience());
        r.setMenuSelections(req.getMenuSelections() != null ? req.getMenuSelections() : List.of());
        r.setAllergySelections(req.getAllergySelections() != null ? req.getAllergySelections() : List.of());
        r.setSpecialRequests(req.getSpecialRequests() != null ? req.getSpecialRequests() : "");

        list.add(0, r);
        reservationRepo.write(list);
        return r;
    }

    public List<Reservation> listReservations(User user) {
        return reservationRepo.read().stream()
                .filter(r -> user.getId().equals(r.getClientId()))
                .toList();
    }

    public Order createOrder(User user, ClientOrderRequest req) {
        List<Order> orders = orderRepo.read();
        List<MenuItem> menu = menuRepo.read();

        // Build a simple id→item map using the proper getter
        Map<Integer, MenuItem> menuIndex = new HashMap<>();
        for (MenuItem m : menu) {
            if (m.getId() != null) {
                menuIndex.put(m.getId(), m);
            }
        }

        List<Order.OrderItem> items = new ArrayList<>();
        for (ClientOrderRequest.OrderItemRef ref : req.getItems()) {
            MenuItem menuItem = menuIndex.get(ref.getId());
            if (menuItem == null) {
                throw new AppException("Menu item " + ref.getId() + " not found", 400);
            }
            Order.OrderItem item = new Order.OrderItem();
            item.setId(ref.getId());
            item.setName(menuItem.getName() != null ? menuItem.getName() : "Unknown");
            item.setQuantity(ref.getQuantity());
            item.setPrice(menuItem.getPrice() != null ? menuItem.getPrice() : 0.0);
            item.setImage(menuItem.getImage() != null ? menuItem.getImage() : "");
            items.add(item);
        }

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        total = Math.round(total * 100.0) / 100.0;

        String orderNumber = "NOIR-" + String.valueOf(Year.now().getValue()).substring(2)
                + "-" + String.valueOf(System.currentTimeMillis()).substring(7);

        List<Order.OrderStep> steps = new ArrayList<>(List.of(
            step("Order Confirmed",  "Your order has been received",       "done",    "fa-check"),
            step("Being Prepared",   "Our kitchen is preparing your order", "pending", "fa-check"),
            step("Out for Delivery", "Delivery rider will contact you",     "pending", "fa-check"),
            step("Delivered",        "Enjoy your meal",                     "pending", "fa-check")
        ));

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setClientId(user.getId());
        order.setCustomerName(
            req.getOrderName() != null && !req.getOrderName().isBlank()
                ? req.getOrderName().trim()
                : (user.getFirstName() + " " + user.getLastName()).trim()
        );
        order.setCustomerEmail(user.getEmail());
        order.setStatus("confirmed");
        order.setEta("35 mins");
        order.setDeliveryAddress(req.getDeliveryAddress());
        order.setDeliveryTime(req.getDeliveryTime());
        order.setContactPhone(req.getContactPhone());
        order.setNotes(req.getNotes() != null ? req.getNotes() : "");
        order.setSteps(steps);
        order.setItems(items);
        order.setTotal(total);
        order.setCreatedAt(Instant.now().toString());

        orders.add(0, order);
        orderRepo.write(orders);
        return order;
    }

    public List<Order> listOrders(User user) {
        return orderRepo.read().stream()
                .filter(o -> user.getId().equals(o.getClientId()))
                .toList();
    }

    public Order cancelOrder(User user, String orderNumber) {
        List<Order> orders = orderRepo.read();
        int idx = -1;
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (user.getId().equals(order.getClientId())
                    && orderNumber.equalsIgnoreCase(order.getOrderNumber())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) throw new AppException("Order not found", 404);

        Order order = orders.get(idx);
        String status = order.getStatus() != null ? order.getStatus().trim().toLowerCase() : "";
        if ("cancelled".equals(status) || "rejected".equals(status) || "delivered".equals(status)) {
            throw new AppException("Order cannot be cancelled", 400);
        }

        order.setStatus("cancelled");
        order.setKitchenNotes("Cancelled by client");
        if (order.getSteps() != null) {
            order.getSteps().forEach(s -> {
                if (!"done".equals(s.getState())) s.setState("cancelled");
            });
        }

        orderRepo.write(orders);
        return order;
    }

    public Order confirmOrderDelivery(User user, String orderNumber) {
        List<Order> orders = orderRepo.read();
        Order order = orders.stream()
                .filter(o -> user.getId().equals(o.getClientId())
                        && o.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst()
                .orElseThrow(() -> new AppException("Order " + orderNumber + " not found", 404));

        String status = order.getStatus() != null ? order.getStatus().trim().toLowerCase() : "";
        if (!"delivered".equals(status)) {
            throw new AppException("Order must be delivered before confirmation", 400);
        }

        order.setStatus("completed");
        order.setKitchenNotes("Delivery confirmed by client");
        orderRepo.write(orders);
        return order;
    }

    public Order trackOrder(User user, String orderNumber) {
        return orderRepo.read().stream()
                .filter(o -> user.getId().equals(o.getClientId())
                        && o.getOrderNumber().equalsIgnoreCase(orderNumber))
                .findFirst()
                .orElseThrow(() -> new AppException("Order " + orderNumber + " not found", 404));
    }

    public Reservation cancelReservation(User user, String reservationId) {
        List<Reservation> reservations = reservationRepo.read();
        int idx = -1;
        for (int i = 0; i < reservations.size(); i++) {
            Reservation r = reservations.get(i);
            if (user.getId().equals(r.getClientId()) && reservationId.equals(r.getId())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) throw new AppException("Reservation not found", 404);

        Reservation reservation = reservations.get(idx);
        String status = reservation.getStatus() != null
                ? reservation.getStatus().trim().toLowerCase() : "";
        if ("cancelled".equals(status) || "rejected".equals(status)) {
            throw new AppException("Reservation cannot be cancelled", 400);
        }

        reservation.setStatus("cancelled");
        reservation.setRejectedReason("Cancelled by client");

        if (reservation.getTableId() != null) {
            try {
                List<TableInfo> tables = tableRepo.read();
                tables.stream()
                    .filter(t -> reservation.getTableId().equals(t.getId()))
                    .findFirst()
                    .ifPresent(table -> table.setAvailable(true));
                tableRepo.write(tables);
            } catch (Exception ignored) { /* tables.json may not exist */ }
        }

        reservationRepo.write(reservations);
        return reservation;
    }

    public Review createReview(User user, ClientReviewRequest req) {
        List<Review> reviews = reviewRepo.read();
        String name = (user.getFirstName() + " " + user.getLastName()).trim();
        String avatar = Arrays.stream(name.split(" "))
                .limit(2)
                .map(p -> p.isEmpty() ? "" : String.valueOf(Character.toUpperCase(p.charAt(0))))
                .reduce("", String::concat);

        Review r = new Review();
        r.setId(UUID.randomUUID().toString());
        r.setClientId(user.getId());
        r.setClientName(name);
        r.setClientEmail(user.getEmail());
        r.setName(name);
        r.setAvatar(avatar);
        r.setPlatform("Client Portal");
        r.setCreatedAt(Instant.now().toString());
        r.setDate(req.getDate());
        r.setRating(req.getRating());
        r.setText(req.getText());

        reviews.add(0, r);
        reviewRepo.write(reviews);
        return r;
    }

    public Map<String, Object> getLoyalty(User user) {
        List<Order> userOrders = orderRepo.read().stream()
                .filter(o -> user.getId().equals(o.getClientId())).toList();
        List<Reservation> userReservations = reservationRepo.read().stream()
                .filter(r -> user.getId().equals(r.getClientId())).toList();

        int orderPoints = userOrders.stream()
                .mapToInt(o -> (int) Math.round(o.getTotal() != null ? o.getTotal() : 0)).sum();
        int reservationPoints = userReservations.size() * 80;
        int totalPoints = orderPoints + reservationPoints;

        String tier = totalPoints >= 5000 ? "Platinum"
                : totalPoints >= 2500 ? "Gold"
                : totalPoints >= 1000 ? "Silver" : "Bronze";
        int nextTarget = "Bronze".equals(tier) ? 1000 : "Silver".equals(tier) ? 2500 : 5000;
        int toNext = Math.max(nextTarget - totalPoints, 0);

        List<Map<String, Object>> activity = new ArrayList<>();
        userOrders.stream().limit(4).forEach(o -> {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("label", "Order " + o.getOrderNumber());
            a.put("points", (int) Math.round(o.getTotal() != null ? o.getTotal() : 0));
            a.put("type", "order");
            a.put("createdAt", o.getCreatedAt());
            activity.add(a);
        });
        userReservations.stream().limit(4).forEach(r -> {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("label", "Reservation " + r.getConfirmationCode());
            a.put("points", 80);
            a.put("type", "reservation");
            a.put("createdAt", r.getCreatedAt());
            activity.add(a);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("points",            totalPoints);
        result.put("tier",              tier);
        result.put("reservationsCount", userReservations.size());
        result.put("ordersCount",       userOrders.size());
        result.put("pointsToNextTier",  toNext);
        result.put("activity",          activity.stream().limit(6).toList());
        return result;
    }

    public List<Map<String, Object>> getOffers() {
        return promotionRepo.read().stream()
                .filter(p -> "active".equals(p.getStatus()))
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          p.getId());
                    m.put("code",        p.getCode());
                    m.put("title",       p.getTitle());
                    m.put("discount",    p.getDiscount());
                    m.put("description", p.getDescription());
                    return m;
                }).toList();
    }

    public Map<String, String> getChef() {
        return Map.of(
            "name",  "Karim Bennani",
            "bio",   "With over 20 years of experience across Paris, Lyon, and Tunis, Chef Karim blends French elegance with Mediterranean flavors.",
            "quote", "Cuisine is the art of bringing people together through taste, tradition, and innovation."
        );
    }

    public Map<String, Object> getAbout() {
        Map<String, Object> about = new LinkedHashMap<>();
        about.put("title", "About NOIR");
        about.put("story", "Since 2015, NOIR has delivered over a decade of refined dining in Tunis. Across 12+ years, our team has blended French technique, Mediterranean products, and warm hospitality to create memorable evenings for local and international guests.");
        about.put("values", List.of(
            "Ingredient quality and traceability",
            "Warm, attentive service",
            "Craftsmanship in every plate",
            "12+ years of culinary consistency and innovation"
        ));
        return about;
    }

    private Order.OrderStep step(String label, String time, String state, String icon) {
        Order.OrderStep s = new Order.OrderStep();
        s.setLabel(label);
        s.setTime(time);
        s.setState(state);
        s.setIcon(icon);
        return s;
    }
}
