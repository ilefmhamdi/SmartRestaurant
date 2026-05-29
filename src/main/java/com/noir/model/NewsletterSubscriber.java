package com.noir.model;

public class NewsletterSubscriber {
    private String id;
    private String email;
    private String subscribedAt;
    private String source;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(String subscribedAt) { this.subscribedAt = subscribedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
