package com.noir.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Get the public directory path from config, with fallback
        String publicDir = appConfig.getPublicDir();
        if (publicDir == null || publicDir.trim().isEmpty()) {
            publicDir = "public";
        }
        
        // Build absolute path for the public directory - handle Windows path separators
        String baseDir = System.getProperty("user.dir");
        String fullPath;
        if (baseDir.contains("\\")) {
            // Windows
            fullPath = baseDir + "\\" + publicDir + "\\";
        } else {
            // Unix/Mac
            fullPath = baseDir + "/" + publicDir + "/";
        }
        
        // For Windows, we need file:/ or file:/ and proper format
        if (fullPath.contains("\\")) {
            fullPath = "file:///" + fullPath.replace("\\", "/");
        }
        
        // Serve public/ directory as static files at root
        registry.addResourceHandler("/**")
                .addResourceLocations(fullPath);
    }
}