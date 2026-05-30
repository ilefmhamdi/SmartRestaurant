package com.noir.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String publicDir = appConfig.getPublicDir();
        if (publicDir == null || publicDir.trim().isEmpty()) {
            publicDir = "public";
        }

        String baseDir = System.getProperty("user.dir");
        String fullPath;
        if (baseDir.contains("\\")) {
            fullPath = "file:///" + (baseDir + "\\" + publicDir + "\\").replace("\\", "/");
        } else {
            fullPath = "file:" + baseDir + "/" + publicDir + "/";
        }

        // Serve static assets only on specific extensions — never on /**
        // This prevents the resource handler from intercepting PUT/PATCH/DELETE /api/** calls
        registry.addResourceHandler("/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg", "/*.svg", "/*.woff", "/*.woff2")
                .addResourceLocations(fullPath);
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}