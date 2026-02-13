package com.easymoney.market.infrastructure.persistence;

import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockPriceRepositoryImpl implements StockPriceRepository {

    private final JpaStockPriceRepository jpaStockPriceRepository;

    @Override
    public StockPrice save(StockPrice stockPrice) {
        return jpaStockPriceRepository.save(stockPrice);
    }

    @Override
    public Optional<StockPrice> findByStockCodeAndTradingDate(String stockCode, LocalDate tradingDate) {
        return jpaStockPriceRepository.findByStockCodeAndTradingDate(stockCode, tradingDate);
    }

    @Override
    public List<StockPrice> findByStockCode(String stockCode) {
        return jpaStockPriceRepository.findByStockCode(stockCode);
    }
}
