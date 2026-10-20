<script setup lang="ts">
import { useId } from 'vue'

withDefaults(
  defineProps<{
    label: string
    placeholder?: string
    hint?: string
    error?: string
    rows?: number
    maxlength?: number
  }>(),
  { placeholder: undefined, hint: undefined, error: undefined, rows: 4, maxlength: undefined },
)

const model = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="ml-1 text-[15px] font-semibold">{{ label }}</label>
    <textarea
      :id="id"
      v-model="model"
      :rows="rows"
      :maxlength="maxlength"
      :placeholder="placeholder"
      :aria-invalid="error ? 'true' : undefined"
      :aria-describedby="error || hint ? `${id}-desc` : undefined"
      class="resize-y rounded-field bg-field px-4 py-3 text-ink placeholder:text-ink-2"
      :class="error ? 'ring-2 ring-danger ring-inset' : ''"
    />
    <p v-if="error" :id="`${id}-desc`" class="ml-1 text-sm font-semibold text-danger" role="alert">
      ✕ {{ error }}
    </p>
    <p v-else-if="hint" :id="`${id}-desc`" class="ml-1 text-sm text-ink-2">{{ hint }}</p>
  </div>
</template>
