package com.easymoney.market.interfaces.event;

import com.easymoney.global.event.AnalysisCompletedEvent;
import com.easymoney.market.application.service.MarketReactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarketEventListenerTest {

    @Mock
    private MarketReactionService marketReactionService;

    @InjectMocks
    private MarketEventListener listener;

    @Test
    void 분석_완료_이벤트_수신시_시장_반응을_추적한다() {
        AnalysisCompletedEvent event = new AnalysisCompletedEvent(
                1L, "005930", "삼성전자", LocalDate.of(2024, 5, 15));

        listener.handle(event);

        verify(marketReactionService).trackReaction(1L, "005930", LocalDate.of(2024, 5, 15));
    }

    @Test
    void 시장_반응_추적_실패시_예외를_잡아_로깅한다() {
        AnalysisCompletedEvent event = new AnalysisCompletedEvent(
                1L, "005930", "삼성전자", LocalDate.of(2024, 5, 15));
        doThrow(new RuntimeException("API 오류"))
                .when(marketReactionService).trackReaction(1L, "005930", LocalDate.of(2024, 5, 15));

        listener.handle(event);

        verify(marketReactionService).trackReaction(1L, "005930", LocalDate.of(2024, 5, 15));
    }
}
