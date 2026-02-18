package com.easymoney.market.application.service;

import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.StockClient;
import com.easymoney.market.domain.repository.StockPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockPriceServiceTest {

    @Mock
    private StockClient stockClient;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @InjectMocks
    private StockPriceService stockPriceService;

    @Test
    void shouldFetchAndSaveCurrentPrice() {
        StockPrice price = createStockPrice("005930", LocalDate.now(), 70000);
        given(stockClient.fetchCurrentPrice("005930")).willReturn(Optional.of(price));
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", price.getTradingDate()))
                .willReturn(Optional.empty());
        given(stockPriceRepository.save(price)).willReturn(price);

        Optional<StockPrice> result = stockPriceService.fetchAndSave("005930");

        assertThat(result).isPresent();
        verify(stockPriceRepository).save(price);
    }

    @Test
    void shouldNotDuplicateSaveExistingPrice() {
        StockPrice price = createStockPrice("005930", LocalDate.now(), 70000);
        StockPrice existing = createStockPrice("005930", LocalDate.now(), 70000);
        given(stockClient.fetchCurrentPrice("005930")).willReturn(Optional.of(price));
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", price.getTradingDate()))
                .willReturn(Optional.of(existing));

        Optional<StockPrice> result = stockPriceService.fetchAndSave("005930");

        assertThat(result).isPresent();
        verify(stockPriceRepository, never()).save(any());
    }

    @Test
    void shouldReturnEmptyOptionalWhenFetchFails() {
        given(stockClient.fetchCurrentPrice("005930")).willReturn(Optional.empty());

        Optional<StockPrice> result = stockPriceService.fetchAndSave("005930");

        assertThat(result).isEmpty();
        verify(stockPriceRepository, never()).save(any());
    }

    private StockPrice createStockPrice(String stockCode, LocalDate date, long closePrice) {
        return StockPrice.builder()
                .stockCode(stockCode)
                .tradingDate(date)
                .openPrice(closePrice)
                .highPrice(closePrice)
                .lowPrice(closePrice)
                .closePrice(closePrice)
                .volume(1000000)
                .build();
    }
}
