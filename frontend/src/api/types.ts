export type DisclosureStatus = "NEW" | "IGNORED" | "PENDING_ANALYSIS" | "ANALYZED"

export type DisclosureCategory =
  | "CORRECTION" | "ATTACHMENT" | "AMENDMENT" | "SHELL_COMPANY"
  | "SHAREHOLDER_MEETING" | "STOCK_OPTION" | "PROXY" | "SECURITIES_REPORT"
  | "PAYMENT_SCHEDULE" | "TRADING_HALT" | "UNFAITHFUL_DISCLOSURE"
  | "MARKET_NOTICE" | "SECURITIES_FILING"
  | "REGULAR_REPORT" | "MATERIAL_EVENT" | "OWNERSHIP_CHANGE" | "TENDER_OFFER"
  | "FINANCIAL_CHANGE" | "DIVIDEND" | "EARNINGS" | "AUDIT_REPORT"
  | "CONTRACT" | "LITIGATION" | "GUARANTEE" | "RELATED_PARTY"
  | "MANAGEMENT_ISSUE" | "CAPITAL_CHANGE" | "OTHER"

export type Sentiment = "POSITIVE" | "NEUTRAL" | "NEGATIVE"

export interface DisclosureInfo {
  id: number
  receiptNumber: string
  corporateName: string
  stockCode: string | null
  title: string
  disclosedAt: string
  documentUrl: string
  status: DisclosureStatus
  category: DisclosureCategory
  createdAt: string | null
}

export interface AnalysisReportInfo {
  id: number
  disclosureId: number
  receiptNumber: string
  corporateName: string
  title: string
  sentiment: Sentiment
  score: number
  summary: string
  analyzedAt: string
}

export interface MarketReactionInfo {
  id: number
  disclosureId: number
  stockCode: string
  priorClose: number
  currentClose: number
  changeRate: number
  trackedAt: string
}

export interface CollectResponse {
  collected: number
}

export interface EnrichedDisclosure extends DisclosureInfo {
  analysis: AnalysisReportInfo | null
  marketReaction: MarketReactionInfo | null
}

export interface DashboardStats {
  totalDisclosures: number
  analyzedCount: number
  positiveCount: number
  negativeCount: number
  averageScore: number | null
}
