<script setup lang="ts">
import { computed, ref } from 'vue'
import type { EventDetails } from '@/api'
import { UiButton } from '@/components/ui'
import { useSessionStore } from '@/stores/session'
import { t } from '@/i18n'
import { buildIcs, canAddToCalendar, eventPlace, icsFileName } from '@/features/events/share'
import { formatEventWhen } from '@/utils/datetime'

const session = useSessionStore()
const props = defineProps<{ event: EventDetails }>()

const notice = ref('')
const url = computed(() => `${location.origin}/events/${props.event.id}`)
const shareable = computed(() => props.event.status === 'published' && !props.event.is_past)
const calendar = computed(() => canAddToCalendar(props.event))

async function share() {
  notice.value = ''
  const place = session.isAuthenticated ? eventPlace(props.event) : props.event.city
  const text = [`${props.event.title} — ${formatEventWhen(props.event.starts_at)}`, place]
    .filter(Boolean)
    .join(', ')
  if (navigator.share) {
    try {
      await navigator.share({ title: props.event.title, text, url: url.value })
      return
    } catch (e) {
      if (e instanceof DOMException && e.name === 'AbortError') return
    }
  }
  try {
    await navigator.clipboard.writeText(url.value)
    notice.value = t('share.copied')
  } catch {
    notice.value = t('share.copyManually', { url: url.value })
  }
}

function addToCalendar() {
  const blob = new Blob([buildIcs(props.event, url.value)], { type: 'text/calendar;charset=utf-8' })
  const href = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = href
  a.download = icsFileName(props.event)
  a.click()
  URL.revokeObjectURL(href)
  notice.value = t('share.icsDone')
}
</script>

<template>
  <section
    v-if="shareable"
    :aria-label="$t('share.group')"
    class="flex flex-col gap-2 rounded-card bg-card p-4"
  >
    <div class="flex flex-wrap gap-2">
      <UiButton variant="ghost" class="min-w-32 flex-1" @click="share">{{
        $t('share.share')
      }}</UiButton>
      <UiButton v-if="calendar" variant="ghost" class="min-w-32 flex-1" @click="addToCalendar">
        {{ $t('share.calendar') }}
      </UiButton>
    </div>
    <p v-if="notice" class="text-center text-sm font-medium break-all text-ink-2" role="status">
      {{ notice }}
    </p>
  </section>
</template>
