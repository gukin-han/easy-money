package dev.gukin.einvestlab.analysis.domain.repository;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisReport;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportRepository {

    AnalysisReport save(AnalysisReport report);

    Optional<AnalysisReport> findByDisclosureId(Long disclosureId);

    List<AnalysisReport> findAll();
}
