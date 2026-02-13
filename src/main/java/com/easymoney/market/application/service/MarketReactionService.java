package com.easymoney.market.application.service;

import com.easymoney.market.application.dto.MarketReactionInfo;
import com.easymoney.market.domain.model.MarketReaction;
import com.easymoney.market.domain.model.StockPrice;
import com.easymoney.market.domain.repository.MarketReactionRepository;
import com.easymoney.market.domain.repository.StockClient;
import com.easymoney.market.domain.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketReactionService {

    private final StockClient stockClient;
    private final StockPriceRepository stockPriceRepository;
    private final MarketReactionRepository marketReactionRepository;

    @Transactional
    public void trackReaction(Long disclosureId, String stockCode, LocalDate disclosureDate) {
        if (stockCode == null || stockCode.isBlank()) {
            log.warn("종목코드 없음 — 시장 반응 추적 건너뜀: disclosureId={}", disclosureId);
            return;
        }

        Optional<MarketReaction> existing = marketReactionRepository.findByDisclosureId(disclosureId);
        if (existing.isPresent()) {
            log.debug("이미 추적된 시장 반응: disclosureId={}", disclosureId);
            return;
        }

        LocalDate priorDate = disclosureDate.minusDays(1);
        Optional<StockPrice> priorPrice = findOrFetch(stockCode, priorDate);
        Optional<StockPrice> currentPrice = findOrFetch(stockCode, disclosureDate);

        if (priorPrice.isEmpty() || currentPrice.isEmpty()) {
            log.warn("주가 데이터 부족 — 시장 반응 추적 실패: stockCode={}, disclosureDate={}",
                    stockCode, disclosureDate);
            return;
        }

        long priorClose = priorPrice.get().getClosePrice();
        long currentClose = currentPrice.get().getClosePrice();
        BigDecimal changeRate = MarketReaction.calculateChangeRate(priorClose, currentClose);

        MarketReaction reaction = MarketReaction.builder()
                .disclosureId(disclosureId)
                .stockCode(stockCode)
                .priorClose(priorClose)
                .currentClose(currentClose)
                .changeRate(changeRate)
                .trackedAt(LocalDateTime.now())
                .build();

        marketReactionRepository.save(reaction);
        log.info("시장 반응 추적 완료: stockCode={}, changeRate={}", stockCode, changeRate);
    }

    @Transactional(readOnly = true)
    public List<MarketReactionInfo> findAll() {
        return marketReactionRepository.findAll().stream()
                .map(MarketReactionInfo::from)
                .toList();
    }

    private Optional<StockPrice> findOrFetch(String stockCode, LocalDate date) {
        Optional<StockPrice> cached = stockPriceRepository.findByStockCodeAndTradingDate(stockCode, date);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<StockPrice> fetched = stockClient.fetchDailyPrice(stockCode, date);
        fetched.ifPresent(stockPriceRepository::save);
        return fetched;
    }
}
