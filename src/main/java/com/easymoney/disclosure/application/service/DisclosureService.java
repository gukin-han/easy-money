package com.easymoney.disclosure.application.service;

import com.easymoney.disclosure.application.dto.DisclosureInfo;
import com.easymoney.disclosure.domain.model.Disclosure;
import com.easymoney.disclosure.domain.repository.DisclosureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisclosureService {

    private final DisclosureRepository disclosureRepository;

    public List<DisclosureInfo> findAll() {
        return disclosureRepository.findAll().stream()
                .map(DisclosureInfo::from)
                .toList();
    }

    @Transactional
    public DisclosureInfo save(Disclosure disclosure) {
        return DisclosureInfo.from(disclosureRepository.save(disclosure));
    }
}
