package dev.gukin.einvestlab.market.infrastructure.persistence;

import dev.gukin.einvestlab.market.domain.model.MarketReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaMarketReactionRepository extends JpaRepository<MarketReaction, Long> {

    Optional<MarketReaction> findByDisclosureId(Long disclosureId);
}
