package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.model.DisclosureCategory;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import com.easymoney.disclosure.domain.service.DisclosureClassifier;
import com.easymoney.global.event.NewDisclosureEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DisclosureCollectionServiceTest {

    @Mock
    private DartClient dartClient;

    @Mock
    private DisclosureRepository disclosureRepository;

    @Mock
    private DisclosureClassifier classifier;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DisclosureCollectionService collectionService;

    @Test
    void 새로운_공시만_저장한다() {
        Disclosure d1 = createDisclosure("001", "사업보고서");
        Disclosure d2 = createDisclosure("002", "사업보고서");
        Disclosure d3 = createDisclosure("003", "사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1, d2, d3));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001", "002", "003")))
                .willReturn(Set.of("001"));
        given(classifier.classify("사업보고서")).willReturn(DisclosureCategory.REGULAR_REPORT);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        int count = collectionService.collect();

        assertThat(count).isEqualTo(2);
        verify(disclosureRepository).save(d2);
        verify(disclosureRepository).save(d3);
        verify(disclosureRepository, never()).save(d1);
    }

    @Test
    void 분석_대상_공시는_이벤트를_발행한다() {
        Disclosure d1 = createDisclosure("001", "사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001")))
                .willReturn(Set.of());
        given(classifier.classify("사업보고서")).willReturn(DisclosureCategory.REGULAR_REPORT);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        collectionService.collect();

        assertThat(d1.getStatus()).isEqualTo(DisclosureStatus.PENDING_ANALYSIS);
        ArgumentCaptor<NewDisclosureEvent> captor = ArgumentCaptor.forClass(NewDisclosureEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        NewDisclosureEvent event = captor.getValue();
        assertThat(event.receiptNumber()).isEqualTo("001");
        assertThat(event.corporateName()).isEqualTo("테스트회사");
        assertThat(event.stockCode()).isEqualTo("005930");
        assertThat(event.disclosureDate()).isEqualTo(java.time.LocalDate.of(2024, 5, 15));
    }

    @Test
    void IGNORED_공시는_이벤트를_발행하지_않는다() {
        Disclosure d1 = createDisclosure("001", "[기재정정]사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001")))
                .willReturn(Set.of());
        given(classifier.classify("[기재정정]사업보고서")).willReturn(DisclosureCategory.CORRECTION);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        collectionService.collect();

        assertThat(d1.getStatus()).isEqualTo(DisclosureStatus.IGNORED);
        verify(disclosureRepository).save(d1);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 혼합_공시_중_분석_대상만_이벤트를_발행한다() {
        Disclosure analyzable = createDisclosure("001", "사업보고서");
        Disclosure ignorable = createDisclosure("002", "[기재정정]사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(analyzable, ignorable));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001", "002")))
                .willReturn(Set.of());
        given(classifier.classify("사업보고서")).willReturn(DisclosureCategory.REGULAR_REPORT);
        given(classifier.classify("[기재정정]사업보고서")).willReturn(DisclosureCategory.CORRECTION);
        given(disclosureRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        int count = collectionService.collect();

        assertThat(count).isEqualTo(2);
        verify(disclosureRepository).save(analyzable);
        verify(disclosureRepository).save(ignorable);
        verify(eventPublisher, times(1)).publishEvent(any(NewDisclosureEvent.class));
    }

    @Test
    void 모두_중복이면_저장하지_않는다() {
        Disclosure d1 = createDisclosure("001", "사업보고서");

        given(dartClient.fetchRecentDisclosures()).willReturn(List.of(d1));
        given(disclosureRepository.findExistingReceiptNumbers(List.of("001")))
                .willReturn(Set.of("001"));

        int count = collectionService.collect();

        assertThat(count).isEqualTo(0);
        verify(disclosureRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 빈_목록이면_저장하지_않는다() {
        given(dartClient.fetchRecentDisclosures()).willReturn(List.of());
        given(disclosureRepository.findExistingReceiptNumbers(List.of()))
                .willReturn(Set.of());

        int count = collectionService.collect();

        assertThat(count).isEqualTo(0);
        verify(disclosureRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
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
