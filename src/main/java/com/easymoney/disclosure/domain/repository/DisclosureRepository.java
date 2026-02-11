package com.easymoney.disclosure.domain.repository;

import com.easymoney.disclosure.domain.model.Disclosure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DisclosureRepository {

    Disclosure save(Disclosure disclosure);

    Optional<Disclosure> findByReceiptNumber(String receiptNumber);

    List<Disclosure> findAll();

    Set<String> findExistingReceiptNumbers(Collection<String> receiptNumbers);
}
