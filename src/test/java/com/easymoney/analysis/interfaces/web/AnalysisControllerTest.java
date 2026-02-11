package com.easymoney.analysis.interfaces.web;

import com.easymoney.analysis.application.dto.AnalysisReportInfo;
import com.easymoney.analysis.application.service.AnalysisService;
import com.easymoney.analysis.domain.model.Sentiment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void findAll_분석_결과를_반환한다() throws Exception {
        List<AnalysisReportInfo> reports = List.of(
                new AnalysisReportInfo(
                        1L, 1L, "20240515000001", "삼성전자", "사업보고서",
                        Sentiment.POSITIVE, 75, "호재성 공시입니다.",
                        LocalDateTime.of(2024, 5, 15, 10, 30)
                )
        );
        given(analysisService.findAll()).willReturn(reports);

        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptNumber").value("20240515000001"))
                .andExpect(jsonPath("$[0].corporateName").value("삼성전자"))
                .andExpect(jsonPath("$[0].sentiment").value("POSITIVE"))
                .andExpect(jsonPath("$[0].score").value(75))
                .andExpect(jsonPath("$[0].summary").value("호재성 공시입니다."));
    }

    @Test
    void findAll_빈_결과를_반환한다() throws Exception {
        given(analysisService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
