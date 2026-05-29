package com.noir.service;

import com.noir.model.MenuItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final MenuService menuService;
    private final OrderService orderService;
    private final ReservationService reservationService;

    public ChatService(MenuService menuService, OrderService orderService, ReservationService reservationService) {
        this.menuService = menuService;
        this.orderService = orderService;
        this.reservationService = reservationService;
    }

    public ChatResponse reply(String message, java.util.List<com.noir.controller.ChatController.ChatMessage> history) {
        ChatResponse res = new ChatResponse();
        if (message == null || message.isBlank()) {
            res.setReply("Hello! I can help with reservations, menu recommendations, order tracking, and restaurant details. What would you like to do today?");
            res.addSuggestion("Show Menu");
            res.addSuggestion("Reserve Table");
            return res;
        }

        String normalized = message.trim().toLowerCase(Locale.ROOT);

        // Reservation intent
        if (matches(normalized, "reserve", "reservation", "book", "table", "dine")) {
            res.setReply("I can help you book a table. Do you want lunch or dinner, and for how many people?");
            res.addSuggestion("Reserve Table");
            res.addSuggestion("Available Times");
            return res;
        }

        // Vegetarian intent
        if (matches(normalized, "veg", "vegetarian", "vegan", "plant-based")) {
            return buildVegetarianResponse();
        }

        // Menu / recommendations
        if (matches(normalized, "menu", "dish", "starter", "main", "dessert", "drink", "wine", "recommend", "special")) {
            return buildMenuResponse(normalized);
        }

        // Hours
        if (matches(normalized, "hour", "open", "close", "opening", "closing")) {
            res.setReply("NOIR is open Monday through Sunday from 12:00 to 00:00. For bookings outside those hours, please call +216 71 240 240.");
            res.addSuggestion("Reserve Table");
            return res;
        }

        // Location / contact
        if (matches(normalized, "address", "location", "where", "avenue", "phone", "contact", "email", "call")) {
            res.setReply("We are on Avenue Bourguiba in Tunis. Call +216 71 240 240 or email reservations@noir-tunis.tn.");
            res.addSuggestion("Get Directions");
            return res;
        }

        // Orders
        if (matches(normalized, "order", "delivery", "takeaway", "track", "status")) {
            res.setReply("To place an order, use the Order button on any dish. To track an existing order, enter your order number in the tracker.");
            res.addSuggestion("Show Menu");
            res.addSuggestion("Track Order");
            return res;
        }

        // Admin/kitchen stats
        if (matches(normalized, "sales", "revenue", "orders", "availability", "capacity", "stock", "kitchen")) {
            int totalOrders = orderService.getRepo().read().size();
            int totalReservations = reservationService.getRepo().read().size();
            res.setReply(String.format("Current system stats: %d delivery orders and %d reservations recorded.", totalOrders, totalReservations));
            return res;
        }

        // Fallback: attempt to match menu items by keyword
        List<MenuItem> matches = menuService.list(null, null, normalized);
        if (!matches.isEmpty()) {
            List<String> names = matches.stream().limit(5).map(MenuItem::getName).collect(Collectors.toList());
            res.setReply("I found some menu items you might like: " + String.join(", ", names) + ". Would you like to order any of them?");
            res.setItems(names);
            res.addSuggestion("Show Menu");
            res.addSuggestion("Order " + names.get(0));
            return res;
        }

        // Default fallback prompt with suggestions to guide user
        res.setReply("I can help with reservations, menu suggestions, order tracking, restaurant hours, and contact details. What would you like to do next?");
        res.addSuggestion("Show Menu");
        res.addSuggestion("Reserve Table");
        res.addSuggestion("Contact Us");
        return res;
    }

    private ChatResponse buildVegetarianResponse() {
        ChatResponse res = new ChatResponse();
        List<MenuItem> vegItems = menuService.list(null, true, null);
        if (vegItems.isEmpty()) {
            res.setReply("We have vegetarian options across the menu. Use the menu filter to browse vegetarian dishes.");
            res.addSuggestion("Show Menu");
            return res;
        }
        List<String> names = vegItems.stream().limit(4).map(MenuItem::getName).collect(Collectors.toList());
        res.setReply("Yes — we offer vegetarian dishes such as " + String.join(", ", names) + ". Would you like to see more?");
        res.setItems(names);
        res.addSuggestion("Show Menu");
        res.addSuggestion("Order " + names.get(0));
        return res;
    }

    private ChatResponse buildMenuResponse(String normalized) {
        ChatResponse res = new ChatResponse();
        List<MenuItem> allItems = menuService.list(null, null, null);
        if (normalized.contains("special") || normalized.contains("recommend") || normalized.contains("signature")) {
            List<String> names = allItems.stream().limit(4).map(MenuItem::getName).collect(Collectors.toList());
            res.setReply("Some of our signature dishes are " + String.join(", ", names) + ". Would you like to view the menu or order?");
            res.setItems(names);
            res.addSuggestion("Show Menu");
            res.addSuggestion("Order " + names.get(0));
            return res;
        }

        List<MenuItem> matches = menuService.list(null, null, normalized);
        if (!matches.isEmpty()) {
            List<String> names = matches.stream().limit(6).map(MenuItem::getName).collect(Collectors.toList());
            res.setReply("I found these items: " + String.join(", ", names) + ". Want to order any?");
            res.setItems(names);
            res.addSuggestion("Show Menu");
            res.addSuggestion("Order " + names.get(0));
            return res;
        }

        res.setReply("Our menu features starters, mains, desserts, and drinks with French-Mediterranean flair. Use the Order button on any dish to place delivery.");
        res.addSuggestion("Show Menu");
        return res;
    }

    private boolean matches(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    public static class ChatResponse {
        private String reply;
        private List<String> suggestions = new ArrayList<>();
        private List<String> items = new ArrayList<>();

        public ChatResponse() {}

        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
        public void addSuggestion(String s) { this.suggestions.add(s); }
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
        public void setItems(java.util.Collection<String> col) { this.items = new ArrayList<>(col); }
    }
}
