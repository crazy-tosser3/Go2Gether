<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'accent' | 'ghost' | 'success'
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
    sparks?: boolean
  }>(),
  { variant: 'primary', type: 'button', disabled: false, loading: false, sparks: false },
)

// Дизайн-система: одна главная (primary) в видимой области, accent — создание контента, ghost — второстепенное.
const variants = {
  primary: 'bg-ink text-white hover:bg-ink/85',
  accent: 'bg-orange text-orange-ink hover:brightness-95',
  ghost: 'bg-card text-ink ring-2 ring-mute ring-inset hover:bg-field',
  success: 'bg-success-solid text-[#ffffff] hover:brightness-95',
}
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :aria-busy="loading || undefined"
    class="inline-flex min-h-13 w-full items-center justify-center gap-2.5 rounded-full px-6 text-base font-bold transition-colors disabled:cursor-not-allowed disabled:bg-mute disabled:text-ink-2"
    :class="variants[variant]"
  >
    <span v-if="sparks" aria-hidden="true" class="text-sm">✦</span>
    <span>{{ loading ? $t('ui.wait') : '' }}<slot v-if="!loading" /></span>
    <span v-if="sparks" aria-hidden="true" class="text-sm">✦</span>
  </button>
</template>
