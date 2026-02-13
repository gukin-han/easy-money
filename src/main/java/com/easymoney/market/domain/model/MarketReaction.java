package com.easymoney.market.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_reaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long disclosureId;

    @Column(nullable = false)
    private String stockCode;

    @Column(nullable = false)
    private long priorClose;

    @Column(nullable = false)
    private long currentClose;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal changeRate;

    @Column(nullable = false)
    private LocalDateTime trackedAt;

    @Builder
    public MarketReaction(Long disclosureId, String stockCode,
                          long priorClose, long currentClose,
                          BigDecimal changeRate, LocalDateTime trackedAt) {
        this.disclosureId = disclosureId;
        this.stockCode = stockCode;
        this.priorClose = priorClose;
        this.currentClose = currentClose;
        this.changeRate = changeRate;
        this.trackedAt = trackedAt;
    }

    public static BigDecimal calculateChangeRate(long priorClose, long currentClose) {
        if (priorClose == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(currentClose - priorClose)
                .divide(BigDecimal.valueOf(priorClose), 4, RoundingMode.HALF_UP);
    }
}
