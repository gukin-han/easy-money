package com.easymoney.disclosure.domain.service;

import com.easymoney.disclosure.domain.model.DisclosureCategory;

public interface DisclosureClassifier {
    DisclosureCategory classify(String title);
}
