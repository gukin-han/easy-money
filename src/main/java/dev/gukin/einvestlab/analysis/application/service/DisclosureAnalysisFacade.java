package dev.gukin.einvestlab.analysis.application.service;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisReport;
import dev.gukin.einvestlab.disclosure.domain.repository.DartClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisclosureAnalysisFacade {

    private final DartClient dartClient;
    private final AnalysisService analysisService;

    public void execute(
        Long disclosureId,
        String receiptNumber,
        String corporateName,
        String title
    ) {
        log.info("새 공시 분석 시작: {} - {}", corporateName, title);

        try {
            String content = dartClient.fetchDocumentContent(receiptNumber);
            AnalysisReport report = analysisService.analyze(disclosureId, receiptNumber, corporateName, title, content);
            analysisService.completeAnalysis(report);
            log.info("공시 분석 완료: {} - {}", corporateName, title);
        } catch (Exception e) {
            log.error("공시 분석 실패: disclosureId={}, {} - {}", disclosureId, corporateName, title, e);
        }
    }
}
