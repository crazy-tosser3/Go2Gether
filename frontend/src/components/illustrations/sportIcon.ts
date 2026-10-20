import type { SportIconName } from './SportIcon.vue'

// Бэк присылает в icon эмодзи, а у нас свои рисунки — выбираем их по названию направления.
const RULES: [RegExp, SportIconName][] = [
  [/футбол/, 'football'],
  [/теннис|бадминтон|сквош|пинг/, 'tennis'],
  [/шахмат|шашк|го\b/, 'chess'],
  [/вело|bmx/, 'bike'],
  [/бег|пробеж|марафон|трейл/, 'running'],
]

export function sportIconName(sport: { name: string }): SportIconName {
  const name = sport.name.toLocaleLowerCase('ru').replace(/ё/g, 'е')
  return RULES.find(([re]) => re.test(name))?.[1] ?? 'custom'
}
