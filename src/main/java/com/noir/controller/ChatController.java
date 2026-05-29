package com.noir.controller;

import com.noir.service.ChatService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        com.noir.service.ChatService.ChatResponse resp = chatService.reply(request.getMessage(), request.getHistory());
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of(
                "reply", resp.getReply(),
                "suggestions", resp.getSuggestions(),
                "items", resp.getItems()
        )));
    }

    public static class ChatRequest {
        @NotBlank
        private String message;
        private java.util.List<ChatMessage> history;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public java.util.List<ChatMessage> getHistory() { return history; }
        public void setHistory(java.util.List<ChatMessage> history) { this.history = history; }
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
