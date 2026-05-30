package com.noir.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noir.controller.ChatController;
import com.noir.model.MenuItem;

@Service
public class ChatService {

    private final MenuService menuService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${groq.api.key}")
    private String groqApiKey;

    public ChatService(MenuService menuService) {
        this.menuService = menuService;
    }

    public ChatResponse reply(String message, List<ChatController.ChatMessage> history) {
        ChatResponse res = new ChatResponse();

        try {
            // Build menu context
            List<MenuItem> allItems = menuService.list(null, null, null);
            StringBuilder menuBuilder = new StringBuilder();
for (int i = 0; i < Math.min(20, allItems.size()); i++) {
    MenuItem item = allItems.get(i);
    menuBuilder.append(item.getName())
               .append(" (").append(item.getCat()).append(")")
               .append(" - ").append(item.getPrice()).append(" TND\n");
}
String menuContext = menuBuilder.toString();

            String systemPrompt = """
                You are a helpful assistant for NOIR, a French-Mediterranean restaurant in Tunis, Tunisia.
                You help customers with menu questions, reservations, orders, and restaurant info.
                Restaurant hours: Monday-Sunday 12:00-00:00.
                Address: Avenue Bourguiba, Tunis. Phone: +216 71 240 240.
                Current menu:
                """ + menuContext + """
                
                Keep replies short and friendly. Always respond in the same language as the customer.
                """;

            // Build messages array
            List<java.util.Map<String, String>> messages = new ArrayList<>();
            messages.add(java.util.Map.of("role", "system", "content", systemPrompt));

            if (history != null) {
                for (ChatController.ChatMessage h : history) {
                    messages.add(java.util.Map.of("role", h.getRole(), "content", h.getContent()));
                }
            }
            messages.add(java.util.Map.of("role", "user", "content", message));

            // Build request body
            String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", messages,
                "max_tokens", 300
            ));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println("GROQ RESPONSE: " + response.body());
var json = objectMapper.readTree(response.body());
String reply = json.at("/choices/0/message/content").asText();
if (reply == null || reply.isBlank()) {
    reply = "Je n'ai pas pu générer une réponse. Réessayez.";
}

            res.setReply(reply);
            res.addSuggestion("Show Menu");
            res.addSuggestion("Reserve Table");

        }  catch (Exception e) {
    e.printStackTrace();
    res.setReply("Sorry, I'm having trouble connecting. Please try again.");
}

        return res;
    }

    public static class ChatResponse {
        private String reply;
        private List<String> suggestions = new ArrayList<>();
        private List<String> items = new ArrayList<>();

        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
        public void addSuggestion(String s) { this.suggestions.add(s); }
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
    }
}