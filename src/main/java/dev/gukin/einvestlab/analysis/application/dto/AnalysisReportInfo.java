package dev.gukin.einvestlab.analysis.application.dto;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisReport;
import dev.gukin.einvestlab.analysis.domain.model.Sentiment;

import java.time.LocalDateTime;

public record AnalysisReportInfo(
        Long id,
        Long disclosureId,
        String receiptNumber,
        String corporateName,
        String title,
        Sentiment sentiment,
        int score,
        String summary,
        LocalDateTime analyzedAt
) {

    public static AnalysisReportInfo from(AnalysisReport report) {
        return new AnalysisReportInfo(
                report.getId(),
                report.getDisclosureId(),
                report.getReceiptNumber(),
                report.getCorporateName(),
                report.getTitle(),
                report.getSentiment(),
                report.getScore(),
                report.getSummary(),
                report.getAnalyzedAt()
        );
    }
}
