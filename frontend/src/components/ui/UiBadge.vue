<script setup lang="ts">
import { computed } from 'vue'
import { toneIcon, type StatusTone } from './status'

const props = withDefaults(defineProps<{ tone?: StatusTone | 'outline' | 'accent' }>(), {
  tone: 'outline',
})

const classes: Record<NonNullable<typeof props.tone>, string> = {
  ok: 'bg-success-bg text-success-fg',
  wait: 'bg-warning-bg text-warning-fg',
  queue: 'bg-queue-bg text-queue-fg',
  no: 'bg-danger-bg text-danger-fg',
  off: 'bg-field text-ink-2 line-through',
  outline: 'bg-field text-ink',
  accent: 'bg-accent text-ink',
}

const icon = computed(() => (props.tone in toneIcon ? toneIcon[props.tone as StatusTone] : ''))
</script>

<template>
  <span
    class="inline-flex w-fit items-center gap-1.5 whitespace-nowrap rounded-full px-3 py-1 text-sm font-bold"
    :class="classes[tone]"
  >
    <span v-if="icon" aria-hidden="true">{{ icon }}</span>
    <slot />
  </span>
</template>
