package com.easymoney.disclosure.domain.service;

import com.easymoney.disclosure.domain.model.DisclosureCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TitleBasedDisclosureClassifierTest {

    private final TitleBasedDisclosureClassifier classifier = new TitleBasedDisclosureClassifier();

    // === IGNORABLE ===

    @Test
    void 기재정정_포함시_CORRECTION으로_분류한다() {
        assertThat(classifier.classify("[기재정정]사업보고서"))
                .isEqualTo(DisclosureCategory.CORRECTION);
    }

    @Test
    void 첨부추가_포함시_ATTACHMENT로_분류한다() {
        assertThat(classifier.classify("첨부추가(사업보고서)"))
                .isEqualTo(DisclosureCategory.ATTACHMENT);
    }

    @Test
    void 증권신고서_정정_포함시_AMENDMENT로_분류한다() {
        assertThat(classifier.classify("증권신고서(채무증권)(정정)"))
                .isEqualTo(DisclosureCategory.AMENDMENT);
    }

    @Test
    void 장외회사_포함시_SHELL_COMPANY로_분류한다() {
        assertThat(classifier.classify("[장외회사]사업보고서"))
                .isEqualTo(DisclosureCategory.SHELL_COMPANY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"주주총회소집결의", "주주총회소집공고", "주주총회집중일개최사유신고"})
    void 주주총회_관련_공시는_SHAREHOLDER_MEETING으로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.SHAREHOLDER_MEETING);
    }

    @Test
    void 주식매수선택권_공시는_STOCK_OPTION으로_분류한다() {
        assertThat(classifier.classify("주식매수선택권부여에관한신고"))
                .isEqualTo(DisclosureCategory.STOCK_OPTION);
    }

    @Test
    void 의결권대리행사_공시는_PROXY로_분류한다() {
        assertThat(classifier.classify("의결권대리행사권유참고서류"))
                .isEqualTo(DisclosureCategory.PROXY);
    }

    @Test
    void 증권발행실적보고서는_SECURITIES_REPORT로_분류한다() {
        assertThat(classifier.classify("증권발행실적보고서"))
                .isEqualTo(DisclosureCategory.SECURITIES_REPORT);
    }

    @Test
    void 지급수단별_지급기간별_공시는_PAYMENT_SCHEDULE로_분류한다() {
        assertThat(classifier.classify("지급수단별ㆍ지급기간별보험금지급현황"))
                .isEqualTo(DisclosureCategory.PAYMENT_SCHEDULE);
    }

    @Test
    void 주권매매거래정지_공시는_TRADING_HALT로_분류한다() {
        assertThat(classifier.classify("주권매매거래정지"))
                .isEqualTo(DisclosureCategory.TRADING_HALT);
    }

    @Test
    void 불성실공시법인지정_공시는_UNFAITHFUL_DISCLOSURE로_분류한다() {
        assertThat(classifier.classify("불성실공시법인지정"))
                .isEqualTo(DisclosureCategory.UNFAITHFUL_DISCLOSURE);
    }

    @Test
    void 기타시장안내_공시는_MARKET_NOTICE로_분류한다() {
        assertThat(classifier.classify("기타시장안내              (관리종목 지정사유 추가 우려 관련 안내)"))
                .isEqualTo(DisclosureCategory.MARKET_NOTICE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[발행조건확정]증권신고서(채무증권)", "증권신고서(채무증권)"})
    void 증권신고서_발행관련_공시는_SECURITIES_FILING으로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.SECURITIES_FILING);
    }

    // === ANALYZABLE ===

    @ParameterizedTest
    @ValueSource(strings = {"사업보고서 (2024.12)", "분기보고서 (2024.09)", "반기보고서 (2024.06)"})
    void 정기보고서_키워드시_REGULAR_REPORT로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.REGULAR_REPORT);
    }

    @Test
    void 주요사항보고_포함시_MATERIAL_EVENT로_분류한다() {
        assertThat(classifier.classify("주요사항보고서(자기주식취득결정)"))
                .isEqualTo(DisclosureCategory.MATERIAL_EVENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"임원ㆍ주요주주특정증권등소유상황보고서", "주식등의대량보유상황보고서", "지분변동보고", "최대주주등의주식보유변동"})
    void 지분변동_키워드시_OWNERSHIP_CHANGE로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.OWNERSHIP_CHANGE);
    }

    @Test
    void 공개매수_포함시_TENDER_OFFER로_분류한다() {
        assertThat(classifier.classify("공개매수신고서"))
                .isEqualTo(DisclosureCategory.TENDER_OFFER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"매출액또는손익구조30%(대규모법인은15%)이상변경", "파생상품거래손실발생"})
    void 재무변동_키워드시_FINANCIAL_CHANGE로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.FINANCIAL_CHANGE);
    }

    @Test
    void 배당결정_공시는_DIVIDEND로_분류한다() {
        assertThat(classifier.classify("현금ㆍ현물배당결정"))
                .isEqualTo(DisclosureCategory.DIVIDEND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"영업(잠정)실적(공정공시)", "연결재무제표기준영업(잠정)실적(공정공시)"})
    void 잠정실적_키워드시_EARNINGS로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.EARNINGS);
    }

    @Test
    void 감사보고서는_AUDIT_REPORT로_분류한다() {
        assertThat(classifier.classify("감사보고서제출"))
                .isEqualTo(DisclosureCategory.AUDIT_REPORT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"단일판매ㆍ공급계약체결", "계약체결(자율공시)", "계약해지(자율공시)"})
    void 계약_키워드시_CONTRACT로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.CONTRACT);
    }

    @Test
    void 소송등의제기_공시는_LITIGATION으로_분류한다() {
        assertThat(classifier.classify("소송등의제기(자율공시)"))
                .isEqualTo(DisclosureCategory.LITIGATION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"타인에대한채무보증결정", "담보제공결정"})
    void 채무보증_담보_키워드시_GUARANTEE로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.GUARANTEE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"특수관계인에대한출자", "동일인등출자계열회사와의거래"})
    void 특수관계인_키워드시_RELATED_PARTY로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.RELATED_PARTY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"관리종목지정", "상장폐지결정", "상장적격성실질심사"})
    void 경영위기_키워드시_MANAGEMENT_ISSUE로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.MANAGEMENT_ISSUE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"유상증자결정", "무상증자결정", "자본감소결정", "자기주식취득결정"})
    void 자본변동_키워드시_CAPITAL_CHANGE로_분류한다(String title) {
        assertThat(classifier.classify(title))
                .isEqualTo(DisclosureCategory.CAPITAL_CHANGE);
    }

    // === OTHER / EDGE CASES ===

    @Test
    void 분류_불가시_OTHER로_분류한다() {
        assertThat(classifier.classify("알수없는공시제목"))
                .isEqualTo(DisclosureCategory.OTHER);
    }

    @Test
    void null_제목은_OTHER로_분류한다() {
        assertThat(classifier.classify(null))
                .isEqualTo(DisclosureCategory.OTHER);
    }

    @Test
    void IGNORABLE_카테고리는_isAnalyzable이_false다() {
        assertThat(DisclosureCategory.CORRECTION.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.ATTACHMENT.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.AMENDMENT.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.SHELL_COMPANY.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.SHAREHOLDER_MEETING.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.STOCK_OPTION.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.PROXY.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.SECURITIES_REPORT.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.PAYMENT_SCHEDULE.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.TRADING_HALT.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.UNFAITHFUL_DISCLOSURE.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.MARKET_NOTICE.isAnalyzable()).isFalse();
        assertThat(DisclosureCategory.SECURITIES_FILING.isAnalyzable()).isFalse();
    }

    @Test
    void ANALYZABLE_카테고리는_isAnalyzable이_true다() {
        assertThat(DisclosureCategory.REGULAR_REPORT.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.MATERIAL_EVENT.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.OWNERSHIP_CHANGE.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.TENDER_OFFER.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.FINANCIAL_CHANGE.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.DIVIDEND.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.EARNINGS.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.AUDIT_REPORT.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.CONTRACT.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.LITIGATION.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.GUARANTEE.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.RELATED_PARTY.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.MANAGEMENT_ISSUE.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.CAPITAL_CHANGE.isAnalyzable()).isTrue();
        assertThat(DisclosureCategory.OTHER.isAnalyzable()).isTrue();
    }
}
