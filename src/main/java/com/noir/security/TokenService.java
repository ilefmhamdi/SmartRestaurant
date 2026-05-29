package com.noir.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noir.exception.AppException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Component
public class TokenService {

    private final ObjectMapper mapper = new ObjectMapper();

    public String sign(Map<String, Object> payload, String secret) {
        try {
            String json = mapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String signature = hmacHex(encodedPayload, secret);
            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new AppException("Token signing failed", 500);
        }
    }

    public Map<String, Object> verify(String token, String secret, String missingMessage) {
        if (token == null || token.isBlank()) {
            throw new AppException(missingMessage, 401);
        }

        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new AppException("Invalid token format", 401);
        }

        String encodedPayload = parts[0];
        String signature = parts[1];
        String expected = hmacHex(encodedPayload, secret);

        if (!expected.equals(signature)) {
            throw new AppException("Invalid token", 401);
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
            Map<String, Object> payload = mapper.readValue(decoded, new TypeReference<Map<String, Object>>() {});

            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < System.currentTimeMillis()) {
                throw new AppException("Session expired", 401);
            }

            return payload;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("Invalid token", 401);
        }
    }

    private String hmacHex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new AppException("HMAC failed", 500);
        }
    }
}