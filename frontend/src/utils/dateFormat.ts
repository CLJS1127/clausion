/**
 * 날짜/시간 표기 공통 유틸.
 * 프로젝트 전역에서 slice(0,10), toLocaleDateString 등이 혼용되던 것을 통일한다.
 * - formatDate     → "2026.06.10"
 * - formatDateTime → "2026.06.10 18:30"
 * - formatMonthDay → "6월 10일" (짧은 표기가 필요한 카드용)
 */

function toDate(value: string | Date | null | undefined): Date | null {
  if (!value) return null;
  const d = value instanceof Date ? value : new Date(value);
  return Number.isNaN(d.getTime()) ? null : d;
}

const pad = (n: number) => String(n).padStart(2, '0');

export function formatDate(value: string | Date | null | undefined): string {
  const d = toDate(value);
  if (!d) return '-';
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`;
}

export function formatDateTime(value: string | Date | null | undefined): string {
  const d = toDate(value);
  if (!d) return '-';
  return `${formatDate(d)} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatMonthDay(value: string | Date | null | undefined): string {
  const d = toDate(value);
  if (!d) return '-';
  return `${d.getMonth() + 1}월 ${d.getDate()}일`;
}
