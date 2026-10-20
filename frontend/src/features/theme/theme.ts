import { ref, watch } from 'vue'

export type ThemeChoice = 'light' | 'dark' | 'system'

export const THEME_OPTIONS: { value: ThemeChoice }[] = [
  { value: 'light' },
  { value: 'dark' },
  { value: 'system' },
]

// Тема — строго необходимая настройка интерфейса: храним в браузере без согласия на cookie (см. политику).
const KEY = 'theme'

export function parseTheme(value: unknown): ThemeChoice {
  return value === 'light' || value === 'dark' ? value : 'system'
}

export function readTheme(): ThemeChoice {
  try {
    return parseTheme(localStorage.getItem(KEY))
  } catch {
    return 'system'
  }
}

export function applyTheme(choice: ThemeChoice, root: HTMLElement = document.documentElement) {
  if (choice === 'system') delete root.dataset.theme
  else root.dataset.theme = choice
}

function saveTheme(choice: ThemeChoice) {
  try {
    if (choice === 'system') localStorage.removeItem(KEY)
    else localStorage.setItem(KEY, choice)
  } catch {
    // Хранилище недоступно (приватный режим) — тема действует до перезагрузки.
  }
}

const theme = ref<ThemeChoice>('system')

export function initTheme() {
  theme.value = readTheme()
  applyTheme(theme.value)
}

watch(theme, (choice) => {
  applyTheme(choice)
  saveTheme(choice)
})

export function useTheme() {
  return theme
}
