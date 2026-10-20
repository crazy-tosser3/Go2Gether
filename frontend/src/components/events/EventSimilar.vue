<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { recommendationsApi, type Id, type Recommendation } from '@/api'
import EventListCard from '@/components/events/EventListCard.vue'
import { reasonLabels } from '@/features/events/recommendations'

const props = defineProps<{ eventId: Id }>()

const items = ref<Recommendation[]>([])

// Блок вспомогательный: ошибку не показываем, просто прячем — карточка мероприятия важнее.
watch(
  () => props.eventId,
  async (id) => {
    items.value = []
    try {
      items.value = await recommendationsApi.similar(id, 4)
    } catch {
      items.value = []
    }
  },
  { immediate: true },
)

const cards = computed(() =>
  items.value.map((r) => ({ event: r.event, reasons: reasonLabels(r.reasons, 'similar') })),
)
</script>

<template>
  <section v-if="cards.length" class="flex flex-col gap-3" aria-labelledby="similar-title">
    <h2 id="similar-title" class="text-[22px]">{{ $t('event.similar') }}</h2>
    <ul class="grid gap-3 md:grid-cols-2 2xl:grid-cols-3">
      <li v-for="{ event, reasons } in cards" :key="event.id" class="flex min-w-0 flex-col gap-1.5">
        <EventListCard :event="event" :reasons="reasons" />
      </li>
    </ul>
  </section>
</template>
