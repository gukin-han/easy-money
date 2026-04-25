package dev.gukin.einvestlab.disclosure.application.service;

import dev.gukin.einvestlab.disclosure.domain.model.Disclosure;
import dev.gukin.einvestlab.disclosure.domain.model.DisclosureCategory;
import dev.gukin.einvestlab.disclosure.domain.model.DisclosureStatus;
import dev.gukin.einvestlab.disclosure.domain.repository.DisclosureRepository;
import dev.gukin.einvestlab.disclosure.domain.service.DisclosureClassifier;
import dev.gukin.einvestlab.global.event.NewDisclosureEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DisclosureProcessor {

    private final DisclosureClassifier classifier;
    private final DisclosureRepository disclosureRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void processAll(List<Disclosure> disclosures) {
        disclosures.forEach(this::classifyAndDispatch);
    }

    private void classifyAndDispatch(Disclosure disclosure) {
        DisclosureCategory category = classifier.classify(disclosure.getTitle());
        disclosure.applyCategory(category);
        Disclosure saved = disclosureRepository.save(disclosure);

        if (saved.getStatus() == DisclosureStatus.PENDING_ANALYSIS) {
            eventPublisher.publishEvent(new NewDisclosureEvent(
                    saved.getId(), saved.getReceiptNumber(),
                    saved.getCorporateName(), saved.getTitle(),
                    saved.getStockCode(),
                    saved.getDisclosedAt().toLocalDate()
            ));
        }
    }
}
