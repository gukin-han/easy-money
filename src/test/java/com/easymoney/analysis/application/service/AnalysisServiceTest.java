package com.easymoney.analysis.application.service;

import com.easymoney.analysis.domain.model.AnalysisReport;
import com.easymoney.analysis.domain.model.AnalysisResult;
import com.easymoney.analysis.domain.model.Sentiment;
import com.easymoney.analysis.domain.repository.AnalysisReportRepository;
import com.easymoney.analysis.domain.repository.LlmClient;
import com.easymoney.global.event.AnalysisCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void LLM_분석_결과를_AnalysisReport로_저장한다() {
        AnalysisResult result = new AnalysisResult(Sentiment.POSITIVE, 75, "호재성 공시입니다.");
        given(llmClient.analyze(anyString(), anyString(), anyString())).willReturn(result);

        analysisService.analyze(1L, "20240515000001", "삼성전자", "사업보고서", "본문 내용",
                "005930", LocalDate.of(2024, 5, 15));

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
    void LLM에_기업명_제목_본문을_전달한다() {
        AnalysisResult result = new AnalysisResult(Sentiment.NEUTRAL, 0, "중립적입니다.");
        given(llmClient.analyze(anyString(), anyString(), anyString())).willReturn(result);

        analysisService.analyze(1L, "001", "삼성전자", "사업보고서", "본문",
                "005930", LocalDate.of(2024, 5, 15));

        verify(llmClient).analyze("삼성전자", "사업보고서", "본문");
    }

    @Test
    void 분석_완료_후_AnalysisCompletedEvent를_발행한다() {
        AnalysisResult result = new AnalysisResult(Sentiment.POSITIVE, 80, "긍정적입니다.");
        given(llmClient.analyze(anyString(), anyString(), anyString())).willReturn(result);

        analysisService.analyze(1L, "20240515000001", "삼성전자", "사업보고서", "본문",
                "005930", LocalDate.of(2024, 5, 15));

        ArgumentCaptor<AnalysisCompletedEvent> captor = ArgumentCaptor.forClass(AnalysisCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        AnalysisCompletedEvent event = captor.getValue();
        assertThat(event.disclosureId()).isEqualTo(1L);
        assertThat(event.stockCode()).isEqualTo("005930");
        assertThat(event.corporateName()).isEqualTo("삼성전자");
        assertThat(event.disclosureDate()).isEqualTo(LocalDate.of(2024, 5, 15));
    }
}
