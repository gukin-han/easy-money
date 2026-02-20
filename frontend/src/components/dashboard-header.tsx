import { CollectButton } from "./collect-button"
import { RefreshIndicator } from "./refresh-indicator"

export function DashboardHeader({
  lastUpdated,
  onRefresh,
}: {
  lastUpdated: Date | null
  onRefresh: () => void
}) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">공시 분석 대시보드</h1>
        <p className="text-muted-foreground">
          DART 공시 수집 / LLM 감성 분석 / 시장 반응 추적
        </p>
      </div>
      <div className="flex items-center gap-4">
        <CollectButton onCollected={onRefresh} />
        <RefreshIndicator lastUpdated={lastUpdated} onRefresh={onRefresh} />
      </div>
    </div>
  )
}
