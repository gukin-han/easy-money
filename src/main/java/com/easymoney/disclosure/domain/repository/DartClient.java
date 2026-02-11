package com.easymoney.disclosure.domain.repository;

import com.easymoney.disclosure.domain.model.Disclosure;

import java.util.List;

public interface DartClient {

    List<Disclosure> fetchRecentDisclosures();
}
