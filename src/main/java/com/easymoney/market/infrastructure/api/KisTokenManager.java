package com.easymoney.market.infrastructure.api;

import com.easymoney.market.infrastructure.api.dto.KisTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@ConditionalOnProperty(name = "kis.api.appkey")
public class KisTokenManager {

    private final RestClient kisRestClient;
    private final String appKey;
    private final String appSecret;

    private final ReentrantLock lock = new ReentrantLock();
    private String accessToken;
    private Instant expiresAt = Instant.MIN;

    public KisTokenManager(RestClient kisRestClient,
                           @Value("${kis.api.appkey}") String appKey,
                           @Value("${kis.api.appsecret}") String appSecret) {
        this.kisRestClient = kisRestClient;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public String getAccessToken() {
        if (isTokenValid()) {
            return accessToken;
        }
        lock.lock();
        try {
            if (isTokenValid()) {
                return accessToken;
            }
            refreshToken();
            return accessToken;
        } finally {
            lock.unlock();
        }
    }

    private boolean isTokenValid() {
        return accessToken != null && Instant.now().plusSeconds(3600).isBefore(expiresAt);
    }

    private void refreshToken() {
        log.info("KIS API 토큰 발급 요청");

        KisTokenResponse response = kisRestClient.post()
                .uri("/oauth2/tokenP")
                .body(Map.of(
                        "grant_type", "client_credentials",
                        "appkey", appKey,
                        "appsecret", appSecret
                ))
                .retrieve()
                .body(KisTokenResponse.class);

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException("KIS API 토큰 발급 실패");
        }

        this.accessToken = response.accessToken();
        this.expiresAt = Instant.now().plusSeconds(response.expiresIn());
        log.info("KIS API 토큰 발급 완료 (만료: {})", expiresAt);
    }
}
