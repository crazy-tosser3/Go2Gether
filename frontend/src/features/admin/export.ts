import type { AdminDashboard, EventSummary, ModerationLog } from '@/api'
import { statusView } from '@/components/ui'
import { intlLocale, t } from '@/i18n'
import { eventStatusSlices, moderationSlices, topBy } from './stats'

type Cell = string | number | null | undefined

// Импорт 1С-Битрикс и Excel в русской локали ждут «;» и UTF-8 с BOM — иначе кириллица и столбцы ломаются.
const SEP = ';'
const BOM = '﻿'

function cell(value: Cell): string {
  let s = value == null ? '' : String(value)
  // Названия пишут пользователи: строку с = + - @ таблица приняла бы за формулу.
  if (/^[=+\-@\t\r]/.test(s)) s = `'${s}`
  return /[";\n\r]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s
}

export function toCsv(rows: Cell[][]): string {
  return BOM + rows.map((r) => r.map(cell).join(SEP)).join('\r\n') + '\r\n'
}

// Формат даты и подписи — на языке интерфейса, в котором админ нажал «Выгрузить».
const dateTime = () =>
  new Intl.DateTimeFormat(intlLocale(), {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })

function organizer(e: EventSummary): string {
  // Имя частного организатора — персональные данные (152-ФЗ), в выгрузку не кладём.
  return e.organizer?.type === 'organization' ? e.organizer.name : t('export.private')
}

export function statsCsv(
  dashboard: AdminDashboard | null,
  events: EventSummary[],
  logs: ModerationLog[],
  now: Date,
): string {
  const rows: Cell[][] = [[t('export.title')], [t('export.exported'), dateTime().format(now)]]

  if (dashboard) {
    rows.push(
      [],
      [t('export.summary'), t('export.value')],
      [t('export.users'), dashboard.users],
      [t('export.orgsPending'), dashboard.pending_organizations],
      [t('export.orgsApproved'), dashboard.approved_organizations],
      ...eventStatusSlices(dashboard).map((s) => [
        t('export.eventsBy', { status: s.label.toLowerCase() }),
        s.value,
      ]),
      [t('export.hiddenComments'), dashboard.hidden_comments],
    )
  }

  const section = (title: string, slices: { label: string; value: number }[]) => {
    rows.push([], [title, t('export.events')], ...slices.map((s) => [s.label, s.value]))
  }
  section(
    t('export.columns.sport'),
    topBy(events, (e) => e.sport?.name, Infinity),
  )
  section(
    t('export.columns.city'),
    topBy(events, (e) => e.city, Infinity),
  )
  rows.push([], [t('export.decision'), t('export.count')])
  rows.push(...moderationSlices(logs).map((s) => [s.label, s.value]))

  rows.push(
    [],
    [
      'id',
      'title',
      'sport',
      'city',
      'start',
      'status',
      'participants',
      'capacity',
      'join',
      'organizer',
    ].map((c) => t(`export.columns.${c}`)),
    ...events.map((e) => [
      e.id,
      e.title,
      e.sport?.name,
      e.city,
      dateTime().format(new Date(e.starts_at)),
      statusView({ kind: 'event', status: e.status }).label,
      e.confirmed_count,
      e.capacity ?? t('export.unlimited'),
      t(`export.join.${e.join_mode}`),
      organizer(e),
    ]),
  )
  return toCsv(rows)
}

export function exportFileName(now: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `go2gether-statistika-${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}.csv`
}

export function downloadText(text: string, fileName: string, type = 'text/csv;charset=utf-8') {
  const url = URL.createObjectURL(new Blob([text], { type }))
  const link = Object.assign(document.createElement('a'), { href: url, download: fileName })
  document.body.append(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
