<script setup lang="ts">
import { nextTick, ref, useId, watch } from 'vue'

withDefaults(defineProps<{ title: string; description?: string }>(), {
  description: undefined,
})
const open = defineModel<boolean>('open', { default: false })

const dialog = ref<HTMLDialogElement>()
const id = useId()

// Нативный <dialog> сам держит фокус внутри, закрывается по Esc и делает фон недоступным.
watch(
  open,
  async (value) => {
    await nextTick()
    const el = dialog.value
    if (!el) return
    if (value && !el.open) el.showModal?.()
    if (!value && el.open) el.close()
  },
  { immediate: true },
)

function onClose() {
  open.value = false
}

function onBackdrop(e: MouseEvent) {
  if (e.target === dialog.value) open.value = false
}
</script>

<template>
  <dialog
    ref="dialog"
    :aria-labelledby="`${id}-title`"
    :aria-describedby="description ? `${id}-desc` : undefined"
    class="m-auto w-[min(440px,calc(100vw-32px))] rounded-2xl bg-card p-0 text-ink backdrop:bg-ink/40"
    @close="onClose"
    @click="onBackdrop"
  >
    <div v-if="open" class="flex flex-col gap-4 p-6">
      <div class="flex items-start justify-between gap-3">
        <h2 :id="`${id}-title`" class="text-xl font-bold leading-tight">
          {{ title }}
        </h2>
        <button
          type="button"
          :aria-label="$t('common.close')"
          class="grid size-11 flex-none place-items-center rounded-full bg-field hover:bg-mute"
          @click="open = false"
        >
          ✕
        </button>
      </div>
      <p v-if="description" :id="`${id}-desc`" class="text-[15px] text-ink-2">{{ description }}</p>
      <slot />
      <div v-if="$slots.actions" class="mt-1 flex flex-col-reverse gap-2 sm:flex-row">
        <slot name="actions" />
      </div>
    </div>
  </dialog>
</template>
