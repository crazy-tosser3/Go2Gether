<script setup lang="ts">
import { useId } from 'vue'

const model = defineModel<boolean>({ default: false })
const id = useId()
</script>

<template>
  <div class="flex items-start gap-2.5">
    <input :id="id" v-model="model" type="checkbox" class="mt-0.5 size-5.5 flex-none accent-ink" />
    <label :for="id" class="text-[15px]"><slot /></label>
  </div>
</template>
