package dev.gukin.einvestlab.analysis.infrastructure.persistence;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisReport;
import dev.gukin.einvestlab.analysis.domain.repository.AnalysisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AnalysisReportRepositoryImpl implements AnalysisReportRepository {

    private final JpaAnalysisReportRepository jpaAnalysisReportRepository;

    @Override
    public AnalysisReport save(AnalysisReport report) {
        return jpaAnalysisReportRepository.save(report);
    }

    @Override
    public Optional<AnalysisReport> findByDisclosureId(Long disclosureId) {
        return jpaAnalysisReportRepository.findByDisclosureId(disclosureId);
    }

    @Override
    public List<AnalysisReport> findAll() {
        return jpaAnalysisReportRepository.findAll();
    }
}
