package com.easymoney.analysis.interfaces.event;

import com.easymoney.analysis.application.service.AnalysisService;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import com.easymoney.global.event.NewDisclosureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisEventListener {

    private final DartClient dartClient;
    private final AnalysisService analysisService;
    private final DisclosureRepository disclosureRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NewDisclosureEvent event) {
        log.info("새 공시 분석 시작: {} - {}", event.corporateName(), event.title());

        String content = dartClient.fetchDocumentContent(event.receiptNumber());
        analysisService.analyze(
                event.disclosureId(),
                event.receiptNumber(),
                event.corporateName(),
                event.title(),
                content
        );

        disclosureRepository.updateStatus(event.disclosureId(), DisclosureStatus.ANALYZED);
        log.info("공시 분석 완료: {} - {}", event.corporateName(), event.title());
    }
}
