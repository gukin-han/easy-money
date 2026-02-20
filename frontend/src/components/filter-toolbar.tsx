import { CATEGORY_LABELS } from "@/constants/labels"
import type { DisclosureCategory, Sentiment } from "@/api/types"

const SENTIMENT_OPTIONS: Array<{ value: Sentiment | "ALL"; label: string }> = [
  { value: "ALL", label: "전체" },
  { value: "POSITIVE", label: "긍정" },
  { value: "NEUTRAL", label: "중립" },
  { value: "NEGATIVE", label: "부정" },
]

interface FilterToolbarProps {
  date: string
  dates: string[]
  search: string
  category: DisclosureCategory | "ALL"
  sentiment: Sentiment | "ALL"
  onDateChange: (date: string) => void
  onSearchChange: (search: string) => void
  onCategoryChange: (category: DisclosureCategory | "ALL") => void
  onSentimentChange: (sentiment: Sentiment | "ALL") => void
}

export function FilterToolbar({
  date,
  dates,
  search,
  category,
  sentiment,
  onDateChange,
  onSearchChange,
  onCategoryChange,
  onSentimentChange,
}: FilterToolbarProps) {
  const currentIdx = dates.indexOf(date)
  const hasPrev = currentIdx < dates.length - 1
  const hasNext = currentIdx > 0

  function goPrev() {
    if (hasPrev) onDateChange(dates[currentIdx + 1])
  }

  function goNext() {
    if (hasNext) onDateChange(dates[currentIdx - 1])
  }

  return (
    <div className="flex flex-wrap items-center gap-3 rounded-md border bg-muted/30 px-3 py-2 text-sm">
      {/* Date navigator */}
      <div className="flex items-center gap-1">
        <button
          onClick={goPrev}
          disabled={!hasPrev}
          className="rounded px-1.5 py-0.5 hover:bg-muted disabled:opacity-30"
        >
          ←
        </button>
        <input
          type="date"
          value={date}
          onChange={(e) => onDateChange(e.target.value)}
          className="rounded border bg-background px-2 py-0.5 text-sm"
        />
        <button
          onClick={goNext}
          disabled={!hasNext}
          className="rounded px-1.5 py-0.5 hover:bg-muted disabled:opacity-30"
        >
          →
        </button>
      </div>

      <span className="text-muted-foreground/40">|</span>

      {/* Text search */}
      <input
        type="text"
        value={search}
        onChange={(e) => onSearchChange(e.target.value)}
        placeholder="기업명/제목 검색"
        className="w-48 rounded border bg-background px-2 py-0.5 text-sm placeholder:text-muted-foreground/60"
      />

      <span className="text-muted-foreground/40">|</span>

      {/* Category dropdown */}
      <select
        value={category}
        onChange={(e) =>
          onCategoryChange(e.target.value as DisclosureCategory | "ALL")
        }
        className="rounded border bg-background px-2 py-0.5 text-sm"
      >
        <option value="ALL">전체 카테고리</option>
        {(
          Object.entries(CATEGORY_LABELS) as [DisclosureCategory, string][]
        ).map(([key, label]) => (
          <option key={key} value={key}>
            {label}
          </option>
        ))}
      </select>

      <span className="text-muted-foreground/40">|</span>

      {/* Sentiment toggle */}
      <div className="flex items-center gap-0.5">
        {SENTIMENT_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            onClick={() => onSentimentChange(opt.value)}
            className={`rounded px-2 py-0.5 text-sm transition-colors ${
              sentiment === opt.value
                ? "bg-foreground text-background"
                : "hover:bg-muted"
            }`}
          >
            {opt.label}
          </button>
        ))}
      </div>
    </div>
  )
}
