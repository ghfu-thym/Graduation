package com.spike.ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    /**
     * RestClient mặc định cho hầu hết MoMo API (timeout 30s)
     * MoMo khuyến nghị tối thiểu 30s để đảm bảo nhận phản hồi.
     */
    @Bean
    @Primary
    public RestClient momoRestClient() {
        return buildRestClient(Duration.ofSeconds(30), Duration.ofSeconds(30));
    }

    /**
     * RestClient riêng cho /v2/gateway/api/tokenization/pay (timeout 10s)
     */
    @Bean
    public RestClient momoTokenizationRestClient() {
        return buildRestClient(Duration.ofSeconds(10), Duration.ofSeconds(10));
    }

    private RestClient buildRestClient(Duration connectTimeout, Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
