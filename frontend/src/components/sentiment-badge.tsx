import { Badge } from "@/components/ui/badge"
import type { Sentiment } from "@/api/types"
import { SENTIMENT_LABELS } from "@/constants/labels"

const VARIANT_MAP: Record<Sentiment, "default" | "secondary" | "destructive"> = {
  POSITIVE: "default",
  NEUTRAL: "secondary",
  NEGATIVE: "destructive",
}

const COLOR_CLASS: Record<Sentiment, string> = {
  POSITIVE: "bg-emerald-600 hover:bg-emerald-600",
  NEUTRAL: "",
  NEGATIVE: "",
}

export function SentimentBadge({ sentiment }: { sentiment: Sentiment }) {
  return (
    <Badge variant={VARIANT_MAP[sentiment]} className={COLOR_CLASS[sentiment]}>
      {SENTIMENT_LABELS[sentiment]}
    </Badge>
  )
}
