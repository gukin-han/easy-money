package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DisclosureCollectorTest {

    @Mock
    private DartClient dartClient;

    @Mock
    private DisclosureRepository disclosureRepository;

    @InjectMocks
    private DisclosureCollector collector;

    @Test
    void shouldReturnOnlyNewDisclosures() {
        Disclosure d1 = createDisclosure("001", "사업보고서");
        Disclosure d2 = createDisclosure("002", "사업보고서");
        Disclosure d3 = createDisclosure("003", "사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1, d2, d3));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001", "002", "003")))
                .willReturn(Set.of("001"));

        List<Disclosure> result = collector.fetchNew();

        assertThat(result).containsExactly(d2, d3);
    }

    @Test
    void shouldReturnEmptyListWhenAllDuplicated() {
        Disclosure d1 = createDisclosure("001", "사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001")))
                .willReturn(Set.of("001"));

        List<Disclosure> result = collector.fetchNew();

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenFetchReturnsEmpty() {
        given(dartClient.fetchRecentDisclosures()).willReturn(List.of());
        given(disclosureRepository.findExistingReceiptNumbers(List.of()))
                .willReturn(Set.of());

        List<Disclosure> result = collector.fetchNew();

        assertThat(result).isEmpty();
    }

    private Disclosure createDisclosure(String receiptNumber, String title) {
        return Disclosure.builder()
                .receiptNumber(receiptNumber)
                .corporateName("테스트회사")
                .stockCode("005930")
                .title(title)
                .disclosedAt(LocalDateTime.of(2024, 5, 15, 0, 0))
                .documentUrl("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + receiptNumber)
                .build();
    }
}
