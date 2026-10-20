<script setup lang="ts">
import { computed, ref, useId, watch } from 'vue'
import { creatableLabel, filterOptions, type UiOption } from './options'

const props = withDefaults(
  defineProps<{
    label: string
    options: UiOption[]
    placeholder?: string
    hint?: string
    error?: string
    required?: boolean
    allowCreate?: boolean
  }>(),
  {
    placeholder: undefined,
    hint: undefined,
    error: undefined,
    required: false,
    allowCreate: false,
  },
)
const model = defineModel<string | null>({ default: null })
const emit = defineEmits<{ create: [label: string] }>()

const id = useId()
const listId = `${id}-list`
const query = ref('')
const open = ref(false)
const active = ref(0)

const selected = computed(() => props.options.find((o) => o.value === model.value) ?? null)
const filtered = computed(() => filterOptions(props.options, query.value))
const createText = computed(() =>
  props.allowCreate ? creatableLabel(props.options, query.value) : null,
)
const count = computed(() => filtered.value.length + (createText.value ? 1 : 0))

watch(selected, (o) => (query.value = o?.label ?? ''), { immediate: true })
watch(query, () => (active.value = 0))

function optionId(i: number) {
  return `${id}-opt-${i}`
}

function show() {
  open.value = true
}

function close() {
  open.value = false
  query.value = selected.value?.label ?? ''
}

function choose(i: number) {
  if (i < filtered.value.length) {
    model.value = filtered.value[i]!.value
    query.value = filtered.value[i]!.label
    open.value = false
  } else if (createText.value) {
    emit('create', createText.value)
    open.value = false
  }
}

function move(step: number) {
  if (!open.value) return show()
  if (count.value === 0) return
  active.value = (active.value + step + count.value) % count.value
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    move(1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    move(-1)
  } else if (e.key === 'Enter' && open.value && count.value > 0) {
    e.preventDefault()
    choose(active.value)
  } else if (e.key === 'Escape' && open.value) {
    e.preventDefault()
    close()
  }
}

function onBlur(e: FocusEvent) {
  const next = e.relatedTarget as Node | null
  if (next && (e.currentTarget as HTMLElement).parentElement?.contains(next)) return
  close()
}
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="ml-1 text-[15px] font-semibold">
      {{ label }}<span v-if="required" aria-hidden="true" class="text-orange"> *</span>
    </label>
    <div class="relative">
      <div
        class="flex min-h-13.5 items-center gap-2 rounded-field bg-field px-4 focus-within:bg-card focus-within:outline-2 focus-within:outline-accent-strong"
        :class="error ? 'ring-2 ring-danger ring-inset' : ''"
        @focusout="onBlur"
      >
        <input
          :id="id"
          v-model="query"
          role="combobox"
          autocomplete="off"
          :placeholder="placeholder ?? $t('ui.startTyping')"
          :required="required"
          :aria-expanded="open ? 'true' : 'false'"
          :aria-controls="listId"
          aria-autocomplete="list"
          :aria-activedescendant="open && count > 0 ? optionId(active) : undefined"
          :aria-invalid="error ? 'true' : undefined"
          :aria-describedby="error || hint ? `${id}-desc` : undefined"
          class="min-w-0 flex-1 bg-transparent text-ink outline-none placeholder:text-ink-2"
          @focus="show"
          @input="show"
          @keydown="onKey"
        />
        <span aria-hidden="true" class="text-ink-2">▾</span>
      </div>
      <ul
        v-show="open"
        :id="listId"
        role="listbox"
        :aria-label="label"
        class="absolute inset-x-0 top-full z-20 mt-2 max-h-64 overflow-y-auto rounded-field bg-card p-1.5 shadow-[0_12px_32px_rgba(14,14,16,0.14)]"
      >
        <li
          v-for="(o, i) in filtered"
          :id="optionId(i)"
          :key="o.value"
          role="option"
          :aria-selected="o.value === model ? 'true' : 'false'"
          class="flex cursor-pointer items-center justify-between rounded-md px-3.5 py-2.5"
          :class="i === active ? 'bg-accent-soft' : ''"
          @mousedown.prevent="choose(i)"
          @mousemove="active = i"
        >
          <span>{{ o.label }}</span>
          <span v-if="o.value === model" aria-hidden="true" class="font-bold text-accent-ink"
            >✓</span
          >
        </li>
        <li
          v-if="createText"
          :id="optionId(filtered.length)"
          role="option"
          aria-selected="false"
          class="cursor-pointer rounded-md px-3.5 py-2.5 font-bold"
          :class="active === filtered.length ? 'bg-accent-soft' : ''"
          @mousedown.prevent="choose(filtered.length)"
          @mousemove="active = filtered.length"
        >
          {{ $t('ui.addOwn', { text: createText }) }}
        </li>
        <li v-if="count === 0" class="px-3 py-2 text-ink-2">{{ $t('ui.nothingFound') }}</li>
      </ul>
    </div>
    <p v-if="error" :id="`${id}-desc`" class="ml-1 text-sm font-semibold text-danger" role="alert">
      ✕ {{ error }}
    </p>
    <p v-else-if="hint" :id="`${id}-desc`" class="ml-1 text-sm text-ink-2">{{ hint }}</p>
  </div>
</template>
