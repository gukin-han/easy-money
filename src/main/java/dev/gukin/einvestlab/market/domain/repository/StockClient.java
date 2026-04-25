package dev.gukin.einvestlab.market.domain.repository;

import dev.gukin.einvestlab.market.domain.model.StockPrice;

import java.time.LocalDate;
import java.util.Optional;

public interface StockClient {

    Optional<StockPrice> fetchCurrentPrice(String stockCode);

    Optional<StockPrice> fetchDailyPrice(String stockCode, LocalDate date);
}
