import type { MarketReactionInfo } from "@/api/types"

export function MarketReactionCell({
  reaction,
}: {
  reaction: MarketReactionInfo | null
}) {
  if (!reaction) {
    return <span className="text-muted-foreground">-</span>
  }

  const pct = (reaction.changeRate * 100).toFixed(2)
  const isPositive = reaction.changeRate > 0
  const isNegative = reaction.changeRate < 0

  return (
    <span
      className={
        isPositive
          ? "font-medium text-red-600"
          : isNegative
            ? "font-medium text-blue-600"
            : "text-muted-foreground"
      }
    >
      {isPositive ? `▲ +${pct}%` : isNegative ? `▼ ${pct}%` : `${pct}%`}
    </span>
  )
}
