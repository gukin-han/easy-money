package dev.gukin.einvestlab.market.infrastructure.persistence;

import dev.gukin.einvestlab.market.domain.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaStockPriceRepository extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findByStockCodeAndTradingDate(String stockCode, LocalDate tradingDate);

    List<StockPrice> findByStockCode(String stockCode);
}
