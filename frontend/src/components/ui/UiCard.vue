<script setup lang="ts">
withDefaults(defineProps<{ as?: string }>(), { as: 'div' })
</script>

<template>
  <component :is="as" class="flex flex-col gap-2 rounded-card bg-card p-4">
    <slot />
  </component>
</template>
