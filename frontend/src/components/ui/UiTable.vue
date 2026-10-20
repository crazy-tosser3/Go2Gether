<script setup lang="ts" generic="T">
import { computed } from 'vue'

export interface UiColumn {
  key: string
  label: string
  class?: string
}

const props = withDefaults(
  defineProps<{
    caption: string
    columns: UiColumn[]
    rows: T[]
    rowKey: (row: T) => string
    selectedKey?: string | null
    clickable?: boolean
    loading?: boolean
    emptyText?: string
    cardAside?: string
  }>(),
  {
    selectedKey: null,
    clickable: false,
    loading: false,
    emptyText: undefined,
    cardAside: 'status',
  },
)
const emit = defineEmits<{ select: [row: T] }>()

defineSlots<{
  [K: `cell-${string}`]: (p: { row: T }) => unknown
  empty?: () => unknown
}>()

// На телефоне строка — карточка: первая колонка — заголовок, cardAside — справа, остальные — список.
const cardTitle = computed(() => props.columns[0])
const cardAsideColumn = computed(() => props.columns.find((c) => c.key === props.cardAside))
const cardRest = computed(() =>
  props.columns.slice(1).filter((c) => c.key !== cardAsideColumn.value?.key),
)

function cell(row: T, key: string): unknown {
  return (row as Record<string, unknown>)[key]
}

function pick(row: T) {
  if (props.clickable) emit('select', row)
}
</script>

<template>
  <ul class="flex flex-col gap-2 md:hidden" :aria-label="caption" :aria-busy="loading || undefined">
    <template v-if="loading">
      <li
        v-for="i in 3"
        :key="i"
        class="h-24 animate-pulse rounded-card bg-card"
        aria-hidden="true"
      />
    </template>
    <li v-else-if="rows.length === 0" class="rounded-card bg-card px-4 py-8 text-center text-ink-2">
      <slot name="empty">{{ emptyText ?? $t('ui.empty') }}</slot>
    </li>
    <template v-else>
      <li
        v-for="row in rows"
        :key="rowKey(row)"
        class="relative flex flex-col gap-2 rounded-card p-4"
        :class="rowKey(row) === selectedKey ? 'bg-accent-soft' : 'bg-card'"
      >
        <div class="flex items-start justify-between gap-3">
          <button
            v-if="clickable && cardTitle"
            type="button"
            class="min-w-0 text-left after:absolute after:inset-0 after:rounded-card focus-visible:outline-none focus-visible:after:outline-2 focus-visible:after:outline-accent-strong"
            :aria-current="rowKey(row) === selectedKey ? 'true' : undefined"
            @click="pick(row)"
          >
            <slot :name="`cell-${cardTitle.key}`" :row="row">{{ cell(row, cardTitle.key) }}</slot>
          </button>
          <div v-else-if="cardTitle" class="min-w-0">
            <slot :name="`cell-${cardTitle.key}`" :row="row">{{ cell(row, cardTitle.key) }}</slot>
          </div>
          <div v-if="cardAsideColumn" class="shrink-0">
            <slot :name="`cell-${cardAsideColumn.key}`" :row="row">
              {{ cell(row, cardAsideColumn.key) }}
            </slot>
          </div>
        </div>
        <dl v-if="cardRest.length" class="grid grid-cols-[auto_1fr] gap-x-3 gap-y-0.5 text-[15px]">
          <template v-for="c in cardRest" :key="c.key">
            <dt class="text-ink-2">{{ c.label }}</dt>
            <dd class="min-w-0">
              <slot :name="`cell-${c.key}`" :row="row">{{ cell(row, c.key) }}</slot>
            </dd>
          </template>
        </dl>
      </li>
    </template>
  </ul>

  <div class="hidden overflow-x-auto md:block">
    <table
      class="w-full min-w-[640px] border-separate border-spacing-y-1.5 text-[15px]"
      :aria-busy="loading || undefined"
    >
      <caption class="sr-only">
        {{
          caption
        }}
      </caption>
      <thead>
        <tr>
          <th
            v-for="c in columns"
            :key="c.key"
            scope="col"
            class="px-4 py-1.5 text-left text-xs font-bold tracking-wider text-ink-2 uppercase"
            :class="c.class"
          >
            {{ c.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <template v-if="loading">
          <tr v-for="i in 3" :key="i" aria-hidden="true">
            <td
              v-for="c in columns"
              :key="c.key"
              class="bg-field px-4 py-3.5 first:rounded-l-full last:rounded-r-full"
            >
              <div class="h-4 w-3/4 rounded-md bg-mute" />
            </td>
          </tr>
        </template>
        <tr v-else-if="rows.length === 0">
          <td
            :colspan="columns.length"
            class="rounded-card bg-field px-4 py-8 text-center text-ink-2"
          >
            <slot name="empty">{{ emptyText ?? $t('ui.empty') }}</slot>
          </td>
        </tr>
        <template v-else>
          <tr
            v-for="row in rows"
            :key="rowKey(row)"
            :tabindex="clickable ? 0 : undefined"
            :aria-current="rowKey(row) === selectedKey ? 'true' : undefined"
            class="group"
            :class="clickable ? 'cursor-pointer' : ''"
            @click="pick(row)"
            @keydown.enter="pick(row)"
          >
            <td
              v-for="c in columns"
              :key="c.key"
              class="px-4 py-3 align-middle first:rounded-l-full last:rounded-r-full"
              :class="[
                c.class,
                rowKey(row) === selectedKey ? 'bg-accent-soft' : 'bg-field',
                clickable ? 'group-hover:bg-mute' : '',
              ]"
            >
              <slot :name="`cell-${c.key}`" :row="row">{{ cell(row, c.key) }}</slot>
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
