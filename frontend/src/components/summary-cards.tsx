import type { DashboardStats } from "@/api/types"

export function SummaryCards({ stats }: { stats: DashboardStats }) {
  const items = [
    { label: "총 공시", value: stats.totalDisclosures.toLocaleString() },
    { label: "분석 완료", value: stats.analyzedCount.toLocaleString() },
    {
      label: "긍정 / 부정",
      value: `${stats.positiveCount} / ${stats.negativeCount}`,
    },
    {
      label: "평균 점수",
      value: stats.averageScore !== null ? `${stats.averageScore}점` : "-",
    },
  ]

  return (
    <div className="flex flex-wrap items-center gap-x-6 gap-y-1 rounded-md border bg-muted/30 px-4 py-2 text-sm">
      {items.map((item, i) => (
        <div key={item.label} className="flex items-center gap-1.5">
          <span className="text-muted-foreground">{item.label}</span>
          <span className="font-semibold">{item.value}</span>
          {i < items.length - 1 && (
            <span className="ml-4 text-muted-foreground/40">|</span>
          )}
        </div>
      ))}
    </div>
  )
}
