import { useMemo, useState } from "react"
import { TooltipProvider } from "@/components/ui/tooltip"
import { useDashboardData } from "@/hooks/use-dashboard-data"
import { DashboardHeader } from "@/components/dashboard-header"
import { SummaryCards } from "@/components/summary-cards"
import { FilterToolbar } from "@/components/filter-toolbar"
import { DisclosureTable } from "@/components/disclosure-table"
import type { DisclosureCategory, Sentiment, DashboardStats } from "@/api/types"

function toDateString(iso: string): string {
  return iso.slice(0, 10)
}

function App() {
  const { disclosures, loading, error, lastUpdated, refresh } =
    useDashboardData()

  // filter state
  const [date, setDate] = useState("")
  const [search, setSearch] = useState("")
  const [category, setCategory] = useState<DisclosureCategory | "ALL">("ALL")
  const [sentiment, setSentiment] = useState<Sentiment | "ALL">("ALL")

  // unique dates sorted descending (newest first)
  const dates = useMemo(() => {
    const set = new Set(disclosures.map((d) => toDateString(d.disclosedAt)))
    return Array.from(set).sort((a, b) => b.localeCompare(a))
  }, [disclosures])

  // auto-select latest date when data loads and no date is set
  const activeDate = date && dates.includes(date) ? date : dates[0] ?? ""

  // filtered disclosures
  const filtered = useMemo(() => {
    return disclosures.filter((d) => {
      // date filter
      if (activeDate && toDateString(d.disclosedAt) !== activeDate) return false

      // text search
      if (search) {
        const q = search.toLowerCase()
        if (
          !d.corporateName.toLowerCase().includes(q) &&
          !d.title.toLowerCase().includes(q)
        )
          return false
      }

      // category filter
      if (category !== "ALL" && d.category !== category) return false

      // sentiment filter
      if (sentiment !== "ALL") {
        if (!d.analysis) return false
        if (d.analysis.sentiment !== sentiment) return false
      }

      return true
    })
  }, [disclosures, activeDate, search, category, sentiment])

  // stats from filtered data
  const stats: DashboardStats = useMemo(() => {
    const analyzed = filtered.filter((d) => d.analysis !== null)
    const positive = analyzed.filter(
      (d) => d.analysis!.sentiment === "POSITIVE",
    ).length
    const negative = analyzed.filter(
      (d) => d.analysis!.sentiment === "NEGATIVE",
    ).length
    const avgScore =
      analyzed.length > 0
        ? Math.round(
            analyzed.reduce((sum, d) => sum + d.analysis!.score, 0) /
              analyzed.length,
          )
        : null

    return {
      totalDisclosures: filtered.length,
      analyzedCount: analyzed.length,
      positiveCount: positive,
      negativeCount: negative,
      averageScore: avgScore,
    }
  }, [filtered])

  return (
    <TooltipProvider>
      <div className="min-h-screen bg-background">
        <div className="space-y-4 px-6 py-4">
          <DashboardHeader lastUpdated={lastUpdated} onRefresh={refresh} />
          <SummaryCards stats={stats} />
          <FilterToolbar
            date={activeDate}
            dates={dates}
            search={search}
            category={category}
            sentiment={sentiment}
            onDateChange={setDate}
            onSearchChange={setSearch}
            onCategoryChange={setCategory}
            onSentimentChange={setSentiment}
          />
          {error && (
            <div className="rounded-md border border-destructive/50 bg-destructive/10 p-4 text-sm text-destructive">
              {error}
            </div>
          )}
          <DisclosureTable disclosures={filtered} loading={loading} />
        </div>
      </div>
    </TooltipProvider>
  )
}

export default App
