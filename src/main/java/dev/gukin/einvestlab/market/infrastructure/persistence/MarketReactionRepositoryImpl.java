package dev.gukin.einvestlab.market.infrastructure.persistence;

import dev.gukin.einvestlab.market.domain.model.MarketReaction;
import dev.gukin.einvestlab.market.domain.repository.MarketReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MarketReactionRepositoryImpl implements MarketReactionRepository {

    private final JpaMarketReactionRepository jpaMarketReactionRepository;

    @Override
    public MarketReaction save(MarketReaction marketReaction) {
        return jpaMarketReactionRepository.save(marketReaction);
    }

    @Override
    public Optional<MarketReaction> findByDisclosureId(Long disclosureId) {
        return jpaMarketReactionRepository.findByDisclosureId(disclosureId);
    }

    @Override
    public List<MarketReaction> findAll() {
        return jpaMarketReactionRepository.findAll();
    }
}
