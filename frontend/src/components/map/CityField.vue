<script setup lang="ts">
import { ref, useId, watch } from 'vue'
import { suggestCities, type CitySuggestion } from '@/features/map/citySuggest'

const props = withDefaults(
  defineProps<{ label: string; placeholder?: string; hint?: string; error?: string }>(),
  { placeholder: undefined, hint: undefined, error: undefined },
)
const model = defineModel<string>({ default: '' })

const id = useId()
const listId = `${id}-list`
const open = ref(false)
const active = ref(-1)
const items = ref<CitySuggestion[]>([])
const loading = ref(false)

// Подсказки — после паузы в наборе: на каждую букву запрос не шлём.
let timer: ReturnType<typeof setTimeout> | undefined
let controller: AbortController | null = null
function request(q: string) {
  clearTimeout(timer)
  controller?.abort()
  if (q.trim().length < 2) {
    items.value = []
    loading.value = false
    return
  }
  loading.value = true
  timer = setTimeout(async () => {
    controller = new AbortController()
    try {
      items.value = await suggestCities(q, controller.signal)
      active.value = -1
    } catch (e) {
      if (!(e instanceof DOMException && e.name === 'AbortError')) items.value = []
    } finally {
      loading.value = false
    }
  }, 250)
}

function onInput(e: Event) {
  // Значение модели обновится только после ответа родителя — запрос строим по самому полю.
  const value = (e.target as HTMLInputElement).value
  model.value = value
  open.value = true
  request(value)
}

function choose(i: number) {
  const c = items.value[i]
  if (!c) return
  model.value = c.name
  open.value = false
  items.value = []
}

function onKey(e: KeyboardEvent) {
  const n = items.value.length
  if (e.key === 'ArrowDown' && n) {
    e.preventDefault()
    open.value = true
    active.value = (active.value + 1) % n
  } else if (e.key === 'ArrowUp' && n) {
    e.preventDefault()
    active.value = (active.value - 1 + n) % n
  } else if (e.key === 'Enter' && open.value && active.value >= 0) {
    e.preventDefault()
    choose(active.value)
  } else if (e.key === 'Escape' && open.value) {
    e.preventDefault()
    open.value = false
  }
}

watch(model, (v) => {
  if (!v) items.value = []
})

const optionId = (i: number) => `${id}-opt-${i}`
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="ml-1 text-[15px] font-semibold">{{ label }}</label>
    <div class="relative">
      <input
        :id="id"
        :value="model"
        role="combobox"
        autocomplete="off"
        :placeholder="placeholder"
        :aria-expanded="open && items.length > 0 ? 'true' : 'false'"
        :aria-controls="listId"
        aria-autocomplete="list"
        :aria-activedescendant="open && active >= 0 ? optionId(active) : undefined"
        :aria-invalid="props.error ? 'true' : undefined"
        :aria-describedby="props.error || props.hint ? `${id}-desc` : undefined"
        class="min-h-13.5 w-full rounded-field bg-field px-4 text-ink outline-none placeholder:text-ink-2 focus:bg-card focus:outline-2 focus:outline-accent-strong"
        :class="props.error ? 'ring-2 ring-danger ring-inset' : ''"
        @input="onInput"
        @keydown="onKey"
        @focus="open = true"
        @blur="open = false"
      />
      <ul
        v-show="open && items.length > 0"
        :id="listId"
        role="listbox"
        :aria-label="label"
        class="absolute inset-x-0 top-full z-20 mt-2 max-h-64 overflow-y-auto rounded-field bg-card p-1.5 shadow-[0_12px_32px_rgba(14,14,16,0.14)]"
      >
        <li
          v-for="(c, i) in items"
          :id="optionId(i)"
          :key="c.name + c.region"
          role="option"
          :aria-selected="i === active ? 'true' : 'false'"
          class="flex cursor-pointer flex-col rounded-md px-3.5 py-2"
          :class="i === active ? 'bg-accent-soft' : ''"
          @mousedown.prevent="choose(i)"
          @mousemove="active = i"
        >
          <span class="font-semibold">{{ c.name }}</span>
          <span v-if="c.region" class="text-sm text-ink-2">{{ c.region }}</span>
        </li>
      </ul>
    </div>
    <p
      v-if="props.error"
      :id="`${id}-desc`"
      class="ml-1 text-sm font-semibold text-danger"
      role="alert"
    >
      ✕ {{ props.error }}
    </p>
    <p v-else-if="props.hint" :id="`${id}-desc`" class="ml-1 text-sm text-ink-2">
      {{ props.hint }}
    </p>
    <p class="sr-only" aria-live="polite">
      {{ loading ? '' : open && items.length ? $t('cityField.count', { n: items.length }) : '' }}
    </p>
  </div>
</template>
