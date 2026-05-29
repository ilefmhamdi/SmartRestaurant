package com.noir;

import com.noir.security.AdminAuthFilter;
import com.noir.security.ClientAuthFilter;
import com.noir.security.DeliveryAuthFilter;
import com.noir.security.KitchenAuthFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
public class NoirApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoirApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminFilter(AdminAuthFilter filter) {
        FilterRegistrationBean<AdminAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/admin/*");
        reg.setOrder(1);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<ClientAuthFilter> clientFilter(ClientAuthFilter filter) {
        FilterRegistrationBean<ClientAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/auth/*", "/api/client/*");
        reg.setOrder(2);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<KitchenAuthFilter> kitchenFilter(KitchenAuthFilter filter) {
        FilterRegistrationBean<KitchenAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/kitchen/*");
        reg.setOrder(3);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<DeliveryAuthFilter> deliveryFilter(DeliveryAuthFilter filter) {
        FilterRegistrationBean<DeliveryAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/delivery/*");
        reg.setOrder(4);
        return reg;
    }
}
