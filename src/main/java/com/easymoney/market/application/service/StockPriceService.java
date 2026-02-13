package com.easymoney.market.application.service;

import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.StockClient;
import com.easymoney.market.domain.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    private final StockClient stockClient;
    private final StockPriceRepository stockPriceRepository;

    @Transactional
    public Optional<StockPrice> fetchAndSave(String stockCode) {
        Optional<StockPrice> fetched = stockClient.fetchCurrentPrice(stockCode);

        if (fetched.isEmpty()) {
            return Optional.empty();
        }

        StockPrice price = fetched.get();
        Optional<StockPrice> existing = stockPriceRepository
                .findByStockCodeAndTradingDate(price.getStockCode(), price.getTradingDate());

        if (existing.isPresent()) {
            log.debug("이미 저장된 주가: {} ({})", stockCode, price.getTradingDate());
            return existing;
        }

        StockPrice saved = stockPriceRepository.save(price);
        log.info("주가 저장: {} ({}) 종가={}", stockCode, price.getTradingDate(), price.getClosePrice());
        return Optional.of(saved);
    }
}
