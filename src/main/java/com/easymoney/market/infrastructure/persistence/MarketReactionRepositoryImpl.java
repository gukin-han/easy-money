package com.easymoney.market.infrastructure.persistence;

import com.easymoney.market.domain.model.MarketReaction;
import com.easymoney.market.domain.repository.MarketReactionRepository;
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
