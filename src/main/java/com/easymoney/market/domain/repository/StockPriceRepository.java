package com.easymoney.market.domain.repository;

import com.easymoney.market.domain.model.StockPrice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockPriceRepository {

    StockPrice save(StockPrice stockPrice);

    Optional<StockPrice> findByStockCodeAndTradingDate(String stockCode, LocalDate tradingDate);

    List<StockPrice> findByStockCode(String stockCode);
}
