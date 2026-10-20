<script setup lang="ts">
import { computed } from 'vue'
import type { Slice } from '@/features/admin/stats'

const props = defineProps<{ items: Slice[] }>()

const max = computed(() => Math.max(1, ...props.items.map((i) => i.value)))
</script>

<template>
  <ul class="flex flex-col gap-3">
    <li v-for="item in items" :key="item.key" class="flex flex-col gap-1">
      <div class="flex items-baseline justify-between gap-3">
        <span class="font-medium">{{ item.label }}</span>
        <b class="tabular-nums">{{ item.value }}</b>
      </div>
      <div class="h-3 overflow-hidden rounded-full bg-field" aria-hidden="true">
        <div
          class="h-full rounded-full bg-accent-strong"
          :style="{ width: `${(item.value / max) * 100}%` }"
        />
      </div>
    </li>
  </ul>
</template>
