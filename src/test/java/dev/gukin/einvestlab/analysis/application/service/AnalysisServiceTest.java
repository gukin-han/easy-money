package dev.gukin.einvestlab.analysis.application.service;

import dev.gukin.einvestlab.analysis.domain.model.AnalysisReport;
import dev.gukin.einvestlab.analysis.domain.model.AnalysisResult;
import dev.gukin.einvestlab.analysis.domain.model.Sentiment;
import dev.gukin.einvestlab.analysis.domain.repository.AnalysisReportRepository;
import dev.gukin.einvestlab.analysis.domain.repository.LlmClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private LlmClient llmClient;

    @Mock
    private AnalysisReportRepository analysisReportRepository;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void shouldSaveLlmResultAsAnalysisReport() {
        AnalysisResult result = new AnalysisResult(Sentiment.POSITIVE, 75, "호재성 공시입니다.");
        given(llmClient.analyze(anyString(), anyString(), anyString())).willReturn(result);

        analysisService.analyze(1L, "20240515000001", "삼성전자", "사업보고서", "본문 내용");

        ArgumentCaptor<AnalysisReport> captor = ArgumentCaptor.forClass(AnalysisReport.class);
        verify(analysisReportRepository).save(captor.capture());

        AnalysisReport saved = captor.getValue();
        assertThat(saved.getDisclosureId()).isEqualTo(1L);
        assertThat(saved.getReceiptNumber()).isEqualTo("20240515000001");
        assertThat(saved.getCorporateName()).isEqualTo("삼성전자");
        assertThat(saved.getTitle()).isEqualTo("사업보고서");
        assertThat(saved.getSentiment()).isEqualTo(Sentiment.POSITIVE);
        assertThat(saved.getScore()).isEqualTo(75);
        assertThat(saved.getSummary()).isEqualTo("호재성 공시입니다.");
        assertThat(saved.getAnalyzedAt()).isNotNull();
    }

    @Test
    void shouldPassCorpNameTitleContentToLlm() {
        AnalysisResult result = new AnalysisResult(Sentiment.NEUTRAL, 0, "중립적입니다.");
        given(llmClient.analyze(anyString(), anyString(), anyString())).willReturn(result);

        analysisService.analyze(1L, "001", "삼성전자", "사업보고서", "본문");

        verify(llmClient).analyze("삼성전자", "사업보고서", "본문");
    }
}
