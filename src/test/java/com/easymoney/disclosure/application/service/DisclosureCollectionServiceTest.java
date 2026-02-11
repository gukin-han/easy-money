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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisclosureCollectionServiceTest {

    @Mock
    private DartClient dartClient;

    @Mock
    private DisclosureRepository disclosureRepository;

    @InjectMocks
    private DisclosureCollectionService collectionService;

    @Test
    void 새로운_공시만_저장한다() {
        Disclosure d1 = createDisclosure("001");
        Disclosure d2 = createDisclosure("002");
        Disclosure d3 = createDisclosure("003");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1, d2, d3));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001", "002", "003")))
                .willReturn(Set.of("001"));

        int count = collectionService.collect();

        assertThat(count).isEqualTo(2);
        verify(disclosureRepository).save(d2);
        verify(disclosureRepository).save(d3);
        verify(disclosureRepository, never()).save(d1);
    }

    @Test
    void 모두_중복이면_저장하지_않는다() {
        Disclosure d1 = createDisclosure("001");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001")))
                .willReturn(Set.of("001"));

        int count = collectionService.collect();

        assertThat(count).isEqualTo(0);
        verify(disclosureRepository, never()).save(any());
    }

    @Test
    void 빈_목록이면_저장하지_않는다() {
        given(dartClient.fetchRecentDisclosures()).willReturn(List.of());
        given(disclosureRepository.findExistingReceiptNumbers(List.of()))
                .willReturn(Set.of());

        int count = collectionService.collect();

        assertThat(count).isEqualTo(0);
        verify(disclosureRepository, never()).save(any());
    }

    private Disclosure createDisclosure(String receiptNumber) {
        return Disclosure.builder()
                .receiptNumber(receiptNumber)
                .corporateName("테스트회사")
                .title("테스트공시")
                .disclosedAt(LocalDateTime.of(2024, 5, 15, 0, 0))
                .documentUrl("https://dart.fss.or.kr/dsaf001/main.do?rcpNo=" + receiptNumber)
                .build();
    }
}
