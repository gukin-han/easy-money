import { useState } from "react"
import { Button } from "@/components/ui/button"
import { collectDisclosures } from "@/api/client"

export function CollectButton({ onCollected }: { onCollected: () => void }) {
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [date, setDate] = useState("")

  async function handleClick() {
    setLoading(true)
    setMessage(null)
    try {
      const result = await collectDisclosures(date || undefined)
      setMessage(`${result.collected}건 수집 완료`)
      onCollected()
    } catch {
      setMessage("수집 실패")
    } finally {
      setLoading(false)
      setTimeout(() => setMessage(null), 5000)
    }
  }

  return (
    <div className="flex items-center gap-2">
      <input
        type="date"
        value={date}
        onChange={(e) => setDate(e.target.value)}
        className="h-8 rounded-md border border-input bg-background px-2 text-sm"
      />
      <Button onClick={handleClick} disabled={loading} size="sm">
        {loading ? "수집 중..." : date ? `${date} 수집` : "오늘 수집"}
      </Button>
      {message && (
        <span className="text-sm text-muted-foreground">{message}</span>
      )}
    </div>
  )
}
