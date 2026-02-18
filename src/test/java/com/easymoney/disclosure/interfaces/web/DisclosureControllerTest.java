package com.easymoney.disclosure.interfaces.web;

import com.easymoney.disclosure.application.dto.DisclosureInfo;
import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import com.easymoney.disclosure.application.service.DisclosureService;
import com.easymoney.disclosure.domain.model.DisclosureCategory;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.global.error.DartApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisclosureController.class)
class DisclosureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisclosureService disclosureService;

    @MockitoBean
    private DisclosureCollectionService disclosureCollectionService;

    @Test
    void shouldReturnCollectedCount() throws Exception {
        given(disclosureCollectionService.collect()).willReturn(5);

        mockMvc.perform(post("/api/disclosures/collect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collected").value(5));
    }

    @Test
    void shouldReturnAllDisclosures() throws Exception {
        List<DisclosureInfo> infos = List.of(
                new DisclosureInfo(1L, "001", "테스트회사", "005930", "사업보고서",
                        LocalDateTime.of(2024, 5, 15, 0, 0),
                        "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=001",
                        DisclosureStatus.PENDING_ANALYSIS, DisclosureCategory.REGULAR_REPORT)
        );
        given(disclosureService.findAll()).willReturn(infos);

        mockMvc.perform(get("/api/disclosures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptNumber").value("001"))
                .andExpect(jsonPath("$[0].corporateName").value("테스트회사"));
    }

    @Test
    void shouldReturn502ForDartApiException() throws Exception {
        given(disclosureCollectionService.collect())
                .willThrow(new DartApiException("011", "사용량이 초과되었습니다."));

        mockMvc.perform(post("/api/disclosures/collect"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("DART_API_ERROR"))
                .andExpect(jsonPath("$.message").value("사용량이 초과되었습니다. (status: 011)"));
    }

    @Test
    void shouldReturn502ForRestClientException() throws Exception {
        given(disclosureCollectionService.collect())
                .willThrow(new RestClientException("Connection refused"));

        mockMvc.perform(post("/api/disclosures/collect"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("EXTERNAL_API_ERROR"))
                .andExpect(jsonPath("$.message").value("외부 API 통신 실패: Connection refused"));
    }
}
