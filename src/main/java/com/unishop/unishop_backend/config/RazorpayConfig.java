package com.unishop.unishop_backend.config;

import com.razorpay.RazorpayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    private final String keyId;
    private final String keySecret;

    public RazorpayConfig(@Value("${razorpay.keyId}") String keyId,
                          @Value("${razorpay.keySecret}") String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        String keyId = this.keyId;
        String keySecret = this.keySecret;
        return new RazorpayClient(keyId, keySecret);
    }
}
