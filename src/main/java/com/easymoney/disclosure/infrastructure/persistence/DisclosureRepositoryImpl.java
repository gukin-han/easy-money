package com.easymoney.disclosure.infrastructure.persistence;

import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DisclosureRepositoryImpl implements DisclosureRepository {

    private final JpaDisclosureRepository jpaDisclosureRepository;

    @Override
    public Disclosure save(Disclosure disclosure) {
        return jpaDisclosureRepository.save(disclosure);
    }

    @Override
    public Optional<Disclosure> findByReceiptNumber(String receiptNumber) {
        return jpaDisclosureRepository.findByReceiptNumber(receiptNumber);
    }

    @Override
    public List<Disclosure> findAll() {
        return jpaDisclosureRepository.findAll();
    }
}
