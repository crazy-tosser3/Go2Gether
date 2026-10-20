<script setup lang="ts">
import { useId } from 'vue'

withDefaults(
  defineProps<{
    label: string
    hideLabel?: boolean
    type?: 'text' | 'email' | 'search' | 'password' | 'date' | 'time' | 'number'
    placeholder?: string
    hint?: string
    error?: string
    required?: boolean
    min?: string
  }>(),
  {
    hideLabel: false,
    type: 'text',
    placeholder: undefined,
    hint: undefined,
    error: undefined,
    min: undefined,
  },
)

const model = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="flex flex-col gap-1.5">
    <label :for="id" class="ml-1 text-[15px] font-semibold" :class="{ 'sr-only': hideLabel }">
      {{ label }}<span v-if="required" aria-hidden="true" class="text-orange"> *</span>
    </label>
    <div
      class="flex min-h-13.5 items-center gap-2.5 rounded-field px-4 focus-within:bg-card focus-within:outline-3 focus-within:outline-accent-strong"
      :class="[
        type === 'search' ? 'bg-card shadow-raised' : 'bg-field',
        error ? 'ring-2 ring-danger ring-inset' : '',
      ]"
    >
      <slot name="icon" />
      <input
        :id="id"
        v-model="model"
        :type="type"
        :placeholder="placeholder"
        :required="required"
        :min="min"
        :aria-invalid="error ? 'true' : undefined"
        :aria-describedby="error || hint ? `${id}-desc` : undefined"
        class="min-w-0 flex-1 bg-transparent text-ink outline-none placeholder:text-ink-2"
      />
    </div>
    <p v-if="error" :id="`${id}-desc`" class="ml-1 text-sm font-semibold text-danger" role="alert">
      ✕ {{ error }}
    </p>
    <p v-else-if="hint" :id="`${id}-desc`" class="ml-1 text-sm text-ink-2">{{ hint }}</p>
  </div>
</template>
