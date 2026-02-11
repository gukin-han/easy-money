package com.easymoney.analysis.domain.repository;

import com.easymoney.analysis.domain.model.AnalysisReport;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportRepository {

    AnalysisReport save(AnalysisReport report);

    Optional<AnalysisReport> findByDisclosureId(Long disclosureId);

    List<AnalysisReport> findAll();
}
