import type {
  AnalysisReportInfo,
  CollectResponse,
  DisclosureInfo,
  MarketReactionInfo,
} from "./types"

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init)
  if (!res.ok) {
    const body = await res.text()
    throw new Error(`API error ${res.status}: ${body}`)
  }
  return res.json() as Promise<T>
}

export function getDisclosures() {
  return fetchJson<DisclosureInfo[]>("/api/disclosures")
}

export function getAnalyses() {
  return fetchJson<AnalysisReportInfo[]>("/api/analyses")
}

export function getMarketReactions() {
  return fetchJson<MarketReactionInfo[]>("/api/market-reactions")
}

export function collectDisclosures(date?: string) {
  const url = date
    ? `/api/disclosures/collect?date=${date}`
    : "/api/disclosures/collect"
  return fetchJson<CollectResponse>(url, { method: "POST" })
}
