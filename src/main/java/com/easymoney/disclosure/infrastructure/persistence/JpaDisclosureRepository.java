package com.easymoney.disclosure.infrastructure.persistence;

import com.easymoney.disclosure.domain.model.Disclosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface JpaDisclosureRepository extends JpaRepository<Disclosure, Long> {

    Optional<Disclosure> findByReceiptNumber(String receiptNumber);

    @Query("SELECT d.receiptNumber FROM Disclosure d WHERE d.receiptNumber IN :receiptNumbers")
    Set<String> findReceiptNumbersByReceiptNumberIn(Collection<String> receiptNumbers);
}
