package com.easymoney.market.domain.repository;

import com.easymoney.market.domain.model.StockPrice;

import java.time.LocalDate;
import java.util.Optional;

public interface StockClient {

    Optional<StockPrice> fetchCurrentPrice(String stockCode);

    Optional<StockPrice> fetchDailyPrice(String stockCode, LocalDate date);
}
