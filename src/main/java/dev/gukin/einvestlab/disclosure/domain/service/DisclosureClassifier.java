package dev.gukin.einvestlab.disclosure.domain.service;

import dev.gukin.einvestlab.disclosure.domain.model.DisclosureCategory;

public interface DisclosureClassifier {
    DisclosureCategory classify(String title);
}
