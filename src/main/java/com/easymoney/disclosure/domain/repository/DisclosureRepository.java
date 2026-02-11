package com.easymoney.disclosure.domain.repository;

import com.easymoney.disclosure.domain.model.Disclosure;

import java.util.List;
import java.util.Optional;

public interface DisclosureRepository {

    Disclosure save(Disclosure disclosure);

    Optional<Disclosure> findByReceiptNumber(String receiptNumber);

    List<Disclosure> findAll();
}
