package com.nadiannis.orchestrator_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${service.product.base-url}")
    private String productServiceBaseUrl;

    @Value("${service.payment.base-url}")
    private String paymentServiceBaseUrl;

    @Bean
    public WebClient webClientProduct() {
        return WebClient.builder().baseUrl(productServiceBaseUrl).build();
    }

    @Bean
    public WebClient webClientPayment() {
        return WebClient.builder().baseUrl(paymentServiceBaseUrl).build();
    }

}
