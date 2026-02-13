package com.easymoney.market.infrastructure.api;

import com.easymoney.global.error.KisApiException;
import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.StockClient;
import com.easymoney.market.infrastructure.api.dto.KisDailyPriceItem;
import com.easymoney.market.infrastructure.api.dto.KisDailyPriceResponse;
import com.easymoney.market.infrastructure.api.dto.KisStockPriceOutput;
import com.easymoney.market.infrastructure.api.dto.KisStockPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "kis.api.appkey")
public class KisStockClient implements StockClient {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long RATE_LIMIT_MS = 200;

    private final RestClient kisRestClient;
    private final KisTokenManager tokenManager;
    private final String appKey;
    private final String appSecret;

    public KisStockClient(RestClient kisRestClient,
                          KisTokenManager tokenManager,
                          @Value("${kis.api.appkey}") String appKey,
                          @Value("${kis.api.appsecret}") String appSecret) {
        this.kisRestClient = kisRestClient;
        this.tokenManager = tokenManager;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    @Override
    public Optional<StockPrice> fetchCurrentPrice(String stockCode) {
        try {
            rateLimitSleep();

            KisStockPriceResponse response = kisRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                            .queryParam("FID_INPUT_ISCD", stockCode)
                            .build())
                    .header("authorization", "Bearer " + tokenManager.getAccessToken())
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", "FHKST01010100")
                    .header("content-type", "application/json; charset=utf-8")
                    .retrieve()
                    .body(KisStockPriceResponse.class);

            validateResponse(response);

            KisStockPriceOutput output = response.output();
            if (output == null) {
                return Optional.empty();
            }

            return Optional.of(StockPrice.builder()
                    .stockCode(stockCode)
                    .tradingDate(LocalDate.now())
                    .openPrice(parseLong(output.openPrice()))
                    .highPrice(parseLong(output.highPrice()))
                    .lowPrice(parseLong(output.lowPrice()))
                    .closePrice(parseLong(output.currentPrice()))
                    .volume(parseLong(output.volume()))
                    .build());
        } catch (KisApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("현재가 조회 실패: stockCode={}", stockCode, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<StockPrice> fetchDailyPrice(String stockCode, LocalDate date) {
        try {
            rateLimitSleep();

            String dateStr = date.format(DATE_FORMAT);
            KisDailyPriceResponse response = kisRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
                            .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                            .queryParam("FID_INPUT_ISCD", stockCode)
                            .queryParam("FID_INPUT_DATE_1", dateStr)
                            .queryParam("FID_INPUT_DATE_2", dateStr)
                            .queryParam("FID_PERIOD_DIV_CODE", "D")
                            .queryParam("FID_ORG_ADJ_PRC", "0")
                            .build())
                    .header("authorization", "Bearer " + tokenManager.getAccessToken())
                    .header("appkey", appKey)
                    .header("appsecret", appSecret)
                    .header("tr_id", "FHKST03010100")
                    .header("content-type", "application/json; charset=utf-8")
                    .retrieve()
                    .body(KisDailyPriceResponse.class);

            validateDailyResponse(response);

            if (response.items() == null || response.items().isEmpty()) {
                return Optional.empty();
            }

            KisDailyPriceItem item = response.items().getFirst();
            return Optional.of(StockPrice.builder()
                    .stockCode(stockCode)
                    .tradingDate(LocalDate.parse(item.tradingDate(), DATE_FORMAT))
                    .openPrice(parseLong(item.openPrice()))
                    .highPrice(parseLong(item.highPrice()))
                    .lowPrice(parseLong(item.lowPrice()))
                    .closePrice(parseLong(item.closePrice()))
                    .volume(parseLong(item.volume()))
                    .build());
        } catch (KisApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("일봉 조회 실패: stockCode={}, date={}", stockCode, date, e);
            return Optional.empty();
        }
    }

    private void validateResponse(KisStockPriceResponse response) {
        if (response != null && !"0".equals(response.returnCode())) {
            throw new KisApiException(response.returnCode(), response.message());
        }
    }

    private void validateDailyResponse(KisDailyPriceResponse response) {
        if (response != null && !"0".equals(response.returnCode())) {
            throw new KisApiException(response.returnCode(), response.message());
        }
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Long.parseLong(value.trim());
    }

    private void rateLimitSleep() {
        try {
            Thread.sleep(RATE_LIMIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
