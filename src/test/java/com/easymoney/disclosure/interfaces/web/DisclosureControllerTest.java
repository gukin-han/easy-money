package com.easymoney.disclosure.interfaces.web;

import com.easymoney.disclosure.application.dto.DisclosureInfo;
import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import com.easymoney.disclosure.application.service.DisclosureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    void collect_수집_결과를_반환한다() throws Exception {
        given(disclosureCollectionService.collect()).willReturn(5);

        mockMvc.perform(post("/api/disclosures/collect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collected").value(5));
    }

    @Test
    void findAll_전체_공시를_반환한다() throws Exception {
        List<DisclosureInfo> infos = List.of(
                new DisclosureInfo(1L, "001", "테스트회사", "005930", "사업보고서",
                        LocalDateTime.of(2024, 5, 15, 0, 0),
                        "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=001")
        );
        given(disclosureService.findAll()).willReturn(infos);

        mockMvc.perform(get("/api/disclosures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiptNumber").value("001"))
                .andExpect(jsonPath("$[0].corporateName").value("테스트회사"));
    }
}
