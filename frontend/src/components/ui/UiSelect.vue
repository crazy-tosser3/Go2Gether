<script setup lang="ts">
import { useId } from 'vue'
import type { UiOption } from './options'

withDefaults(
  defineProps<{ label: string; options: UiOption[]; placeholder?: string; hideLabel?: boolean }>(),
  { placeholder: undefined, hideLabel: false },
)
const model = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="ml-1 text-[15px] font-semibold" :class="{ 'sr-only': hideLabel }">{{
      label
    }}</label>
    <div class="relative">
      <select
        :id="id"
        v-model="model"
        class="min-h-13.5 w-full appearance-none rounded-field bg-field py-2 pr-10 pl-4 text-ink"
      >
        <option v-if="placeholder" value="">{{ placeholder }}</option>
        <option v-for="o in options" :key="o.value" :value="o.value">{{ o.label }}</option>
      </select>
      <span
        aria-hidden="true"
        class="pointer-events-none absolute top-1/2 right-3.5 -translate-y-1/2 text-ink-2"
        >▾</span
      >
    </div>
  </div>
</template>
