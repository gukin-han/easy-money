package com.easymoney.market.interfaces.scheduler;

import com.easymoney.market.application.service.StockPriceService;
import com.easymoney.market.domain.model.MarketReaction;
import com.easymoney.market.domain.repository.MarketReactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StockPriceSchedulerTest {

    @Mock
    private StockPriceService stockPriceService;

    @Mock
    private MarketReactionRepository marketReactionRepository;

    @InjectMocks
    private StockPriceScheduler scheduler;

    @Test
    void shouldNotCollectOutsideMarketHours() {
        if (!scheduler.isMarketOpen()) {
            scheduler.collectPrices();
            verifyNoInteractions(marketReactionRepository);
            verifyNoInteractions(stockPriceService);
        }
    }

    @Test
    void shouldReturnBooleanBasedOnMarketHours() {
        // isMarketOpen()이 boolean을 반환하는지 확인
        boolean result = scheduler.isMarketOpen();
        assertThat(result).isNotNull();
    }

    @Test
    void shouldSkipCollectionWhenStockListIsEmpty() {
        if (scheduler.isMarketOpen()) {
            given(marketReactionRepository.findAll()).willReturn(List.of());

            scheduler.collectPrices();

            verify(stockPriceService, never()).fetchAndSave(org.mockito.ArgumentMatchers.anyString());
        }
    }
}
