/** Возврат после входа: только внутренний путь, иначе на главную (защита от открытого редиректа). */
export function safeRedirect(value: unknown): string {
  const path = Array.isArray(value) ? value[0] : value
  if (typeof path !== 'string' || !path.startsWith('/') || path.startsWith('//')) return '/'
  if (path.startsWith('/login')) return '/'
  return path
}
