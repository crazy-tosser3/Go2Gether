<script setup lang="ts">
import { computed } from 'vue'
import UiBadge from './UiBadge.vue'
import { statusView, type StatusOf } from './status'

const props = defineProps<{ of: StatusOf; label?: string }>()

const view = computed(() => statusView(props.of))
</script>

<template>
  <UiBadge :tone="view.tone">{{ label ?? view.label }}</UiBadge>
</template>
