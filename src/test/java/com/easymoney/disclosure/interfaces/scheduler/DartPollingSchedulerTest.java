package com.easymoney.disclosure.interfaces.scheduler;

import com.easymoney.disclosure.application.service.DisclosureCollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DartPollingSchedulerTest {

    @Mock
    private DisclosureCollectionService disclosureCollectionService;

    @InjectMocks
    private DartPollingScheduler scheduler;

    @Test
    void shouldCallCollectOnPoll() {
        given(disclosureCollectionService.collect()).willReturn(3);

        scheduler.poll();

        verify(disclosureCollectionService).collect();
    }

    @Test
    void shouldNotPropagateCollectException() {
        given(disclosureCollectionService.collect()).willThrow(new RuntimeException("API 오류"));

        scheduler.poll();

        verify(disclosureCollectionService).collect();
    }
}
