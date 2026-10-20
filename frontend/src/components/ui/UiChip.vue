<script setup lang="ts">
defineProps<{ pressed?: boolean; count?: number }>()
defineEmits<{ click: [] }>()
</script>

<template>
  <button
    type="button"
    :aria-pressed="pressed ? 'true' : 'false'"
    class="inline-flex flex-none items-center gap-1 whitespace-nowrap rounded-full px-4 py-2 font-semibold transition-colors"
    :class="pressed ? 'bg-ink text-white' : 'bg-field text-ink hover:bg-mute'"
    @click="$emit('click')"
  >
    <slot />
    <span
      v-if="count !== undefined"
      class="tabular-nums"
      :class="pressed ? 'text-mute' : 'text-ink-2'"
      >{{ count }}</span
    >
  </button>
</template>
