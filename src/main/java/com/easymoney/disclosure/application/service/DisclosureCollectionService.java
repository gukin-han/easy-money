package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.model.DisclosureCategory;
import com.easymoney.disclosure.domain.model.DisclosureStatus;
import com.easymoney.disclosure.domain.repository.DartClient;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import com.easymoney.disclosure.domain.service.DisclosureClassifier;
import com.easymoney.global.event.NewDisclosureEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DisclosureCollectionService {

    private final DartClient dartClient;
    private final DisclosureRepository disclosureRepository;
    private final DisclosureClassifier classifier;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public int collect() {
        List<Disclosure> fetched = dartClient.fetchRecentDisclosures();

        List<String> receiptNumbers = fetched.stream()
                .map(Disclosure::getReceiptNumber)
                .toList();

        Set<String> existing = disclosureRepository.findExistingReceiptNumbers(receiptNumbers);

        List<Disclosure> newDisclosures = fetched.stream()
                .filter(d -> !existing.contains(d.getReceiptNumber()))
                .toList();

        newDisclosures.forEach(d -> {
            DisclosureCategory category = classifier.classify(d.getTitle());
            d.applyCategory(category);
            Disclosure saved = disclosureRepository.save(d);

            if (saved.getStatus() == DisclosureStatus.PENDING_ANALYSIS) {
                eventPublisher.publishEvent(new NewDisclosureEvent(
                        saved.getId(), saved.getReceiptNumber(),
                        saved.getCorporateName(), saved.getTitle(),
                        saved.getStockCode(),
                        saved.getDisclosedAt().toLocalDate()
                ));
            }
        });

        return newDisclosures.size();
    }
}
