import type { DisclosureCategory, DisclosureStatus, Sentiment } from "@/api/types"

export const STATUS_LABELS: Record<DisclosureStatus, string> = {
  NEW: "신규",
  IGNORED: "제외",
  PENDING_ANALYSIS: "분석 대기",
  ANALYZED: "분석 완료",
}

export const SENTIMENT_LABELS: Record<Sentiment, string> = {
  POSITIVE: "긍정",
  NEUTRAL: "중립",
  NEGATIVE: "부정",
}

export const CATEGORY_LABELS: Record<DisclosureCategory, string> = {
  CORRECTION: "기재정정",
  ATTACHMENT: "첨부추가",
  AMENDMENT: "증권신고서정정",
  SHELL_COMPANY: "장외회사",
  SHAREHOLDER_MEETING: "주주총회",
  STOCK_OPTION: "주식매수선택권",
  PROXY: "의결권대리행사",
  SECURITIES_REPORT: "증권발행실적보고서",
  PAYMENT_SCHEDULE: "지급수단별지급기간별",
  TRADING_HALT: "주권매매거래정지",
  UNFAITHFUL_DISCLOSURE: "불성실공시법인지정",
  MARKET_NOTICE: "거래소안내",
  SECURITIES_FILING: "증권신고서(발행조건확정)",
  REGULAR_REPORT: "정기보고서",
  MATERIAL_EVENT: "주요사항",
  OWNERSHIP_CHANGE: "지분변동",
  TENDER_OFFER: "공개매수",
  FINANCIAL_CHANGE: "재무변동",
  DIVIDEND: "배당결정",
  EARNINGS: "잠정실적",
  AUDIT_REPORT: "감사보고서",
  CONTRACT: "주요계약",
  LITIGATION: "소송",
  GUARANTEE: "채무보증/담보",
  RELATED_PARTY: "특수관계인거래",
  MANAGEMENT_ISSUE: "경영위기",
  CAPITAL_CHANGE: "자본변동",
  OTHER: "기타",
}
