package com.easymoney.market.domain.repository;

import com.easymoney.market.domain.model.MarketReaction;

import java.util.List;
import java.util.Optional;

public interface MarketReactionRepository {

    MarketReaction save(MarketReaction marketReaction);

    Optional<MarketReaction> findByDisclosureId(Long disclosureId);

    List<MarketReaction> findAll();
}
