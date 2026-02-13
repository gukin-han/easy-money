package com.easymoney.market.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "stock_price", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stockCode", "tradingDate"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String stockCode;

    @Column(nullable = false)
    private LocalDate tradingDate;

    @Column(nullable = false)
    private long openPrice;

    @Column(nullable = false)
    private long highPrice;

    @Column(nullable = false)
    private long lowPrice;

    @Column(nullable = false)
    private long closePrice;

    @Column(nullable = false)
    private long volume;

    @Builder
    public StockPrice(String stockCode, LocalDate tradingDate,
                      long openPrice, long highPrice, long lowPrice,
                      long closePrice, long volume) {
        this.stockCode = stockCode;
        this.tradingDate = tradingDate;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }
}
