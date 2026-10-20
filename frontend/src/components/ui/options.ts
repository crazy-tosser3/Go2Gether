export interface UiOption {
  value: string
  label: string
}

// «ё» и «е» пользователи пишут вперемешку — сравниваем без разницы.
export function normalize(s: string): string {
  return s.trim().toLocaleLowerCase('ru').replace(/ё/g, 'е').replace(/\s+/g, ' ')
}

export function filterOptions(options: UiOption[], query: string): UiOption[] {
  const q = normalize(query)
  if (!q) return options
  const starts: UiOption[] = []
  const contains: UiOption[] = []
  for (const o of options) {
    const l = normalize(o.label)
    if (l.startsWith(q)) starts.push(o)
    else if (l.includes(q)) contains.push(o)
  }
  return [...starts, ...contains]
}

/** Текст для «Добавить своё» или null, если такой вариант уже есть или запрос пуст. */
export function creatableLabel(options: UiOption[], query: string): string | null {
  const q = normalize(query)
  if (!q) return null
  if (options.some((o) => normalize(o.label) === q)) return null
  return query.trim().replace(/\s+/g, ' ')
}
