<script setup lang="ts">
withDefaults(defineProps<{ label: string; round?: boolean; filled?: boolean }>(), {
  round: false,
  filled: false,
})
</script>

<template>
  <button
    type="button"
    :aria-label="label"
    :title="label"
    class="grid size-12 flex-none place-items-center transition-colors"
    :class="[
      round ? 'rounded-full' : 'rounded-2xl',
      filled ? 'bg-ink text-white' : 'bg-field text-ink hover:bg-mute',
    ]"
  >
    <slot />
  </button>
</template>
