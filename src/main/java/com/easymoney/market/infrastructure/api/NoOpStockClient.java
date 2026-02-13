package com.easymoney.market.infrastructure.api;

import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.StockClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
public class NoOpStockClient implements StockClient {

    @Override
    public Optional<StockPrice> fetchCurrentPrice(String stockCode) {
        log.warn("KIS API 미설정 — 현재가 조회 건너뜀: {}", stockCode);
        return Optional.empty();
    }

    @Override
    public Optional<StockPrice> fetchDailyPrice(String stockCode, LocalDate date) {
        log.warn("KIS API 미설정 — 일봉 조회 건너뜀: {} ({})", stockCode, date);
        return Optional.empty();
    }
}
