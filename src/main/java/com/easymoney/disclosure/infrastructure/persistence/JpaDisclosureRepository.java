package com.easymoney.disclosure.infrastructure.persistence;

import com.easymoney.disclosure.domain.model.Disclosure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaDisclosureRepository extends JpaRepository<Disclosure, Long> {

    Optional<Disclosure> findByReceiptNumber(String receiptNumber);
}
