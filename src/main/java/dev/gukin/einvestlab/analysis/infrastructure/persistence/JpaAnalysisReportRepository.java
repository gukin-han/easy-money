package dev.gukin.einvestlab.analysis.infrastructure.persistence;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaAnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {

    Optional<AnalysisReport> findByDisclosureId(Long disclosureId);
}
