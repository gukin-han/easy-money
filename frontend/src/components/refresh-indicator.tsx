import { Button } from "@/components/ui/button"

function formatTime(date: Date) {
  return date.toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  })
}

export function RefreshIndicator({
  lastUpdated,
  onRefresh,
}: {
  lastUpdated: Date | null
  onRefresh: () => void
}) {
  return (
    <div className="flex items-center gap-2 text-sm text-muted-foreground">
      {lastUpdated && <span>마지막 갱신: {formatTime(lastUpdated)}</span>}
      <Button variant="ghost" size="sm" onClick={onRefresh}>
        새로고침
      </Button>
    </div>
  )
}
