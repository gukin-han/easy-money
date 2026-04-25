package dev.gukin.einvestlab.disclosure.domain.repository;

import dev.gukin.einvestlab.disclosure.domain.model.Disclosure;
import dev.gukin.einvestlab.disclosure.domain.model.DisclosureStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DisclosureRepository {

    Disclosure save(Disclosure disclosure);

    List<Disclosure> saveAll(List<Disclosure> disclosures);

    Optional<Disclosure> findByReceiptNumber(String receiptNumber);

    List<Disclosure> findAll();

    Set<String> findExistingReceiptNumbers(Collection<String> receiptNumbers);

    void updateStatus(Long id, DisclosureStatus status);
}
