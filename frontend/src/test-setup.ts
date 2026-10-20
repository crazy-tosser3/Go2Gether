import { config } from '@vue/test-utils'
import { i18n } from '@/i18n'

// Компоненты переводятся через vue-i18n — подключаем его во все монтирования, язык в тестах русский.
config.global.plugins = [...(config.global.plugins ?? []), i18n]
