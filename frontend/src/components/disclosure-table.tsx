import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip"
import type { EnrichedDisclosure } from "@/api/types"
import { CATEGORY_LABELS, STATUS_LABELS } from "@/constants/labels"
import { SentimentBadge } from "./sentiment-badge"
import { MarketReactionCell } from "./market-reaction-cell"

function formatDateTime(iso: string | null) {
  if (!iso) return "-"
  const d = new Date(iso)
  return d.toLocaleString("ko-KR", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  })
}

function TableSkeleton() {
  return (
    <>
      {Array.from({ length: 5 }).map((_, i) => (
        <TableRow key={i}>
          {Array.from({ length: 8 }).map((_, j) => (
            <TableCell key={j}>
              <Skeleton className="h-4 w-full" />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  )
}

export function DisclosureTable({
  disclosures,
  loading,
}: {
  disclosures: EnrichedDisclosure[]
  loading: boolean
}) {
  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[120px]">기업명</TableHead>
            <TableHead>제목</TableHead>
            <TableHead className="w-[100px]">카테고리</TableHead>
            <TableHead className="w-[90px]">상태</TableHead>
            <TableHead className="w-[70px]">감성</TableHead>
            <TableHead className="w-[70px] text-right">점수</TableHead>
            <TableHead className="w-[100px] text-right">시장반응</TableHead>
            <TableHead className="w-[130px]">수집 시각</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableSkeleton />
          ) : disclosures.length === 0 ? (
            <TableRow>
              <TableCell colSpan={8} className="h-24 text-center">
                공시 데이터가 없습니다
              </TableCell>
            </TableRow>
          ) : (
            disclosures.map((d) => (
              <TableRow key={d.id}>
                <TableCell className="font-medium">
                  {d.stockCode ? (
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <span className="cursor-default">{d.corporateName}</span>
                      </TooltipTrigger>
                      <TooltipContent>{d.stockCode}</TooltipContent>
                    </Tooltip>
                  ) : (
                    d.corporateName
                  )}
                </TableCell>
                <TableCell>
                  <a
                    href={d.documentUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:underline"
                  >
                    {d.title}
                  </a>
                </TableCell>
                <TableCell>
                  <span className="text-xs">
                    {CATEGORY_LABELS[d.category] ?? d.category}
                  </span>
                </TableCell>
                <TableCell>
                  <Badge variant="outline" className="text-xs">
                    {STATUS_LABELS[d.status]}
                  </Badge>
                </TableCell>
                <TableCell>
                  {d.analysis ? (
                    <Tooltip>
                      <TooltipTrigger asChild>
                        <span className="cursor-default">
                          <SentimentBadge sentiment={d.analysis.sentiment} />
                        </span>
                      </TooltipTrigger>
                      <TooltipContent className="max-w-sm">
                        {d.analysis.summary}
                      </TooltipContent>
                    </Tooltip>
                  ) : (
                    <span className="text-muted-foreground">-</span>
                  )}
                </TableCell>
                <TableCell className="text-right">
                  {d.analysis ? d.analysis.score : "-"}
                </TableCell>
                <TableCell className="text-right">
                  <MarketReactionCell reaction={d.marketReaction} />
                </TableCell>
                <TableCell className="text-xs">
                  {formatDateTime(d.createdAt)}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  )
}
