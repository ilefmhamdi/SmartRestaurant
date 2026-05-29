package com.noir.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "noir")
public class AppConfig {

    private Admin admin = new Admin();
    private Client client = new Client();
    private Kitchen kitchen = new Kitchen();
    private Delivery delivery = new Delivery();
    private String dataDir;
    private String publicDir;

    public static class Admin {
        private String username;
        private String password;
        private String tokenSecret;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTokenSecret() { return tokenSecret; }
        public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
    }

    public static class Client {
        private String tokenSecret;
        public String getTokenSecret() { return tokenSecret; }
        public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
    }

    public static class Kitchen {
        private String tokenSecret;
        public String getTokenSecret() { return tokenSecret; }
        public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
    }

    public static class Delivery {
        private String tokenSecret;
        public String getTokenSecret() { return tokenSecret; }
        public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }
    }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Kitchen getKitchen() { return kitchen; }
    public void setKitchen(Kitchen kitchen) { this.kitchen = kitchen; }
    public Delivery getDelivery() { return delivery; }
    public void setDelivery(Delivery delivery) { this.delivery = delivery; }
    public String getDataDir() { return dataDir; }
    public void setDataDir(String dataDir) { this.dataDir = dataDir; }
    public String getPublicDir() { return publicDir; }
    public void setPublicDir(String publicDir) { this.publicDir = publicDir; }
}
