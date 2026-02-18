package com.easymoney.market.application.service;

import com.easymoney.market.domain.model.MarketReaction;
import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.MarketReactionRepository;
import com.easymoney.market.domain.repository.StockClient;
import com.easymoney.market.domain.repository.StockPriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarketReactionServiceTest {

    @Mock
    private StockClient stockClient;

    @Mock
    private StockPriceRepository stockPriceRepository;

    @Mock
    private MarketReactionRepository marketReactionRepository;

    @InjectMocks
    private MarketReactionService marketReactionService;

    @Test
    void shouldCalculateAndSaveChangeRate() {
        LocalDate disclosureDate = LocalDate.of(2024, 5, 15);
        LocalDate priorDate = disclosureDate.minusDays(1);
        StockPrice prior = createStockPrice("005930", priorDate, 70000);
        StockPrice current = createStockPrice("005930", disclosureDate, 77000);

        given(marketReactionRepository.findByDisclosureId(1L)).willReturn(Optional.empty());
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", priorDate))
                .willReturn(Optional.of(prior));
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", disclosureDate))
                .willReturn(Optional.of(current));

        marketReactionService.trackReaction(1L, "005930", disclosureDate);

        ArgumentCaptor<MarketReaction> captor = ArgumentCaptor.forClass(MarketReaction.class);
        verify(marketReactionRepository).save(captor.capture());

        MarketReaction saved = captor.getValue();
        assertThat(saved.getDisclosureId()).isEqualTo(1L);
        assertThat(saved.getStockCode()).isEqualTo("005930");
        assertThat(saved.getPriorClose()).isEqualTo(70000);
        assertThat(saved.getCurrentClose()).isEqualTo(77000);
        assertThat(saved.getChangeRate()).isEqualByComparingTo(new BigDecimal("0.1000"));
    }

    @Test
    void shouldFetchFromApiWhenNotInDb() {
        LocalDate disclosureDate = LocalDate.of(2024, 5, 15);
        LocalDate priorDate = disclosureDate.minusDays(1);
        StockPrice prior = createStockPrice("005930", priorDate, 70000);
        StockPrice current = createStockPrice("005930", disclosureDate, 77000);

        given(marketReactionRepository.findByDisclosureId(1L)).willReturn(Optional.empty());
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", priorDate))
                .willReturn(Optional.empty());
        given(stockClient.fetchDailyPrice("005930", priorDate))
                .willReturn(Optional.of(prior));
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", disclosureDate))
                .willReturn(Optional.empty());
        given(stockClient.fetchDailyPrice("005930", disclosureDate))
                .willReturn(Optional.of(current));

        marketReactionService.trackReaction(1L, "005930", disclosureDate);

        verify(stockClient).fetchDailyPrice("005930", priorDate);
        verify(stockClient).fetchDailyPrice("005930", disclosureDate);
        verify(stockPriceRepository).save(prior);
        verify(stockPriceRepository).save(current);
        verify(marketReactionRepository).save(any(MarketReaction.class));
    }

    @Test
    void shouldSkipAlreadyTrackedReaction() {
        MarketReaction existing = MarketReaction.builder()
                .disclosureId(1L)
                .stockCode("005930")
                .priorClose(70000)
                .currentClose(77000)
                .changeRate(new BigDecimal("0.1000"))
                .build();

        given(marketReactionRepository.findByDisclosureId(1L)).willReturn(Optional.of(existing));

        marketReactionService.trackReaction(1L, "005930", LocalDate.of(2024, 5, 15));

        verify(marketReactionRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenStockCodeIsNull() {
        marketReactionService.trackReaction(1L, null, LocalDate.of(2024, 5, 15));

        verify(marketReactionRepository, never()).findByDisclosureId(any());
        verify(marketReactionRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenStockCodeIsEmpty() {
        marketReactionService.trackReaction(1L, "", LocalDate.of(2024, 5, 15));

        verify(marketReactionRepository, never()).findByDisclosureId(any());
        verify(marketReactionRepository, never()).save(any());
    }

    @Test
    void shouldNotSaveWhenPriceDataInsufficient() {
        LocalDate disclosureDate = LocalDate.of(2024, 5, 15);
        LocalDate priorDate = disclosureDate.minusDays(1);

        given(marketReactionRepository.findByDisclosureId(1L)).willReturn(Optional.empty());
        given(stockPriceRepository.findByStockCodeAndTradingDate("005930", priorDate))
                .willReturn(Optional.empty());
        given(stockClient.fetchDailyPrice("005930", priorDate))
                .willReturn(Optional.empty());

        marketReactionService.trackReaction(1L, "005930", disclosureDate);

        verify(marketReactionRepository, never()).save(any());
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
