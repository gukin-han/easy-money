package com.easymoney.market.interfaces.event;

import com.easymoney.global.event.AnalysisCompletedEvent;
import com.easymoney.market.application.service.MarketReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketEventListener {

    private final MarketReactionService marketReactionService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnalysisCompletedEvent event) {
        log.info("분석 완료 이벤트 수신 — 시장 반응 추적 시작: {} ({})",
                event.corporateName(), event.stockCode());

        try {
            marketReactionService.trackReaction(
                    event.disclosureId(),
                    event.stockCode(),
                    event.disclosureDate()
            );
        } catch (Exception e) {
            log.error("시장 반응 추적 실패: disclosureId={}", event.disclosureId(), e);
        }
    }
}
