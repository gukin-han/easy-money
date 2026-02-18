package com.easymoney.analysis.interfaces.event;

import com.easymoney.analysis.application.service.AnalysisService;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import com.easymoney.global.event.NewDisclosureEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalysisEventListenerTest {

    @Mock
    private DartClient dartClient;

    @Mock
    private AnalysisService analysisService;

    @Mock
    private DisclosureRepository disclosureRepository;

    @InjectMocks
    private AnalysisEventListener listener;

    @Test
    void 이벤트_수신시_본문_조회_후_분석을_실행한다() {
        NewDisclosureEvent event = new NewDisclosureEvent(
                1L, "20240515000001", "삼성전자", "사업보고서",
                "005930", LocalDate.of(2024, 5, 15));
        given(dartClient.fetchDocumentContent("20240515000001")).willReturn("공시 본문 텍스트");

        listener.handle(event);

        verify(dartClient).fetchDocumentContent("20240515000001");
        verify(analysisService).analyze(1L, "20240515000001", "삼성전자", "사업보고서", "공시 본문 텍스트");
    }

    @Test
    void 분석_완료_후_상태를_ANALYZED로_업데이트한다() {
        NewDisclosureEvent event = new NewDisclosureEvent(
                1L, "20240515000001", "삼성전자", "사업보고서",
                "005930", LocalDate.of(2024, 5, 15));
        given(dartClient.fetchDocumentContent("20240515000001")).willReturn("공시 본문 텍스트");

        listener.handle(event);

        verify(disclosureRepository).updateStatus(1L, DisclosureStatus.ANALYZED);
    }

    @Test
    void 본문_조회_실패시_빈_문자열로_분석을_실행한다() {
        NewDisclosureEvent event = new NewDisclosureEvent(
                1L, "001", "테스트회사", "테스트공시",
                "005930", LocalDate.of(2024, 5, 15));
        given(dartClient.fetchDocumentContent("001")).willReturn("");

        listener.handle(event);

        verify(analysisService).analyze(1L, "001", "테스트회사", "테스트공시", "");
        verify(disclosureRepository).updateStatus(1L, DisclosureStatus.ANALYZED);
    }

    @Test
    void LLM_호출_실패시_상태를_ANALYZED로_변경하지_않는다() {
        NewDisclosureEvent event = new NewDisclosureEvent(
                1L, "20240515000001", "삼성전자", "사업보고서",
                "005930", LocalDate.of(2024, 5, 15));
        given(dartClient.fetchDocumentContent("20240515000001")).willReturn("공시 본문 텍스트");
        willThrow(new RuntimeException("LLM 호출 실패"))
                .given(analysisService).analyze(1L, "20240515000001", "삼성전자", "사업보고서", "공시 본문 텍스트");

        listener.handle(event);

        verify(disclosureRepository, never()).updateStatus(1L, DisclosureStatus.ANALYZED);
    }

    @Test
    void 본문_조회_예외_발생시_분석을_실행하지_않는다() {
        NewDisclosureEvent event = new NewDisclosureEvent(
                1L, "20240515000001", "삼성전자", "사업보고서",
                "005930", LocalDate.of(2024, 5, 15));
        given(dartClient.fetchDocumentContent("20240515000001"))
                .willThrow(new RuntimeException("DART API 장애"));

        listener.handle(event);

        verify(analysisService, never()).analyze(1L, "20240515000001", "삼성전자", "사업보고서", "");
        verify(disclosureRepository, never()).updateStatus(1L, DisclosureStatus.ANALYZED);
    }
}
