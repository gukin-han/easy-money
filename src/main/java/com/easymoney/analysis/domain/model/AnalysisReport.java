package com.easymoney.analysis.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long disclosureId;

    @Column(nullable = false)
    private String receiptNumber;

    @Column(nullable = false)
    private String corporateName;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentiment sentiment;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    @Builder
    public AnalysisReport(Long disclosureId, String receiptNumber, String corporateName,
                          String title, Sentiment sentiment, int score, String summary,
                          LocalDateTime analyzedAt) {
        this.disclosureId = disclosureId;
        this.receiptNumber = receiptNumber;
        this.corporateName = corporateName;
        this.title = title;
        this.sentiment = sentiment;
        this.score = score;
        this.summary = summary;
        this.analyzedAt = analyzedAt;
    }
}
