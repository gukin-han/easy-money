package com.easymoney.market.infrastructure.persistence;

import com.easymoney.market.domain.model.MarketReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaMarketReactionRepository extends JpaRepository<MarketReaction, Long> {

    Optional<MarketReaction> findByDisclosureId(Long disclosureId);
}
