package com.easymoney.market.infrastructure.persistence;

import com.easymoney.market.domain.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaStockPriceRepository extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findByStockCodeAndTradingDate(String stockCode, LocalDate tradingDate);

    List<StockPrice> findByStockCode(String stockCode);
}
