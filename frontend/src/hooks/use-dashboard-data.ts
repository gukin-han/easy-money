import { useCallback, useEffect, useRef, useState } from "react"
import { getAnalyses, getDisclosures, getMarketReactions } from "@/api/client"
import type {
  DashboardStats,
  EnrichedDisclosure,
} from "@/api/types"

const REFRESH_INTERVAL = 60_000

interface DashboardData {
  disclosures: EnrichedDisclosure[]
  stats: DashboardStats
  loading: boolean
  error: string | null
  lastUpdated: Date | null
  refresh: () => void
}

export function useDashboardData(): DashboardData {
  const [disclosures, setDisclosures] = useState<EnrichedDisclosure[]>([])
  const [stats, setStats] = useState<DashboardStats>({
    totalDisclosures: 0,
    analyzedCount: 0,
    positiveCount: 0,
    negativeCount: 0,
    averageScore: null,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const fetchData = useCallback(async () => {
    try {
      setError(null)
      const [disclosureList, analysisList, reactionList] = await Promise.all([
        getDisclosures(),
        getAnalyses(),
        getMarketReactions(),
      ])

      const analysisMap = new Map(
        analysisList.map((a) => [a.disclosureId, a]),
      )
      const reactionMap = new Map(
        reactionList.map((r) => [r.disclosureId, r]),
      )

      const enriched: EnrichedDisclosure[] = disclosureList.map((d) => ({
        ...d,
        analysis: analysisMap.get(d.id) ?? null,
        marketReaction: reactionMap.get(d.id) ?? null,
      }))

      enriched.sort(
        (a, b) =>
          new Date(b.disclosedAt).getTime() - new Date(a.disclosedAt).getTime(),
      )

      const analyzed = analysisList.length
      const positive = analysisList.filter((a) => a.sentiment === "POSITIVE").length
      const negative = analysisList.filter((a) => a.sentiment === "NEGATIVE").length
      const avgScore =
        analyzed > 0
          ? Math.round(
              analysisList.reduce((sum, a) => sum + a.score, 0) / analyzed,
            )
          : null

      setDisclosures(enriched)
      setStats({
        totalDisclosures: disclosureList.length,
        analyzedCount: analyzed,
        positiveCount: positive,
        negativeCount: negative,
        averageScore: avgScore,
      })
      setLastUpdated(new Date())
    } catch (e) {
      setError(e instanceof Error ? e.message : "데이터를 불러오지 못했습니다")
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchData()
    intervalRef.current = setInterval(fetchData, REFRESH_INTERVAL)
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current)
    }
  }, [fetchData])

  return { disclosures, stats, loading, error, lastUpdated, refresh: fetchData }
}
