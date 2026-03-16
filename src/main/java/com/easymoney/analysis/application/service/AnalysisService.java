package com.easymoney.analysis.application.service;

import com.easymoney.analysis.application.dto.AnalysisReportInfo;
import com.easymoney.analysis.domain.model.AnalysisReport;
import com.easymoney.analysis.domain.model.AnalysisResult;
import com.easymoney.analysis.domain.repository.AnalysisReportRepository;
import com.easymoney.analysis.domain.repository.LlmClient;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final LlmClient llmClient;
    private final AnalysisReportRepository analysisReportRepository;
    private final DisclosureRepository disclosureRepository;

    public AnalysisReport analyze(
        Long disclosureId,
        String receiptNumber,
        String corporateName,
        String title,
        String content
    ) {
        AnalysisResult result = llmClient.analyze(corporateName, title, content);

        return AnalysisReport.builder()
                .disclosureId(disclosureId)
                .receiptNumber(receiptNumber)
                .corporateName(corporateName)
                .title(title)
                .sentiment(result.sentiment())
                .score(result.score())
                .summary(result.summary())
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public void completeAnalysis(AnalysisReport report) {
        analysisReportRepository.save(report);
        disclosureRepository.updateStatus(report.getDisclosureId(), DisclosureStatus.ANALYZED);
    }

    @Transactional(readOnly = true)
    public List<AnalysisReportInfo> findAll() {
        return analysisReportRepository.findAll().stream()
                .map(AnalysisReportInfo::from)
                .toList();
    }
}
