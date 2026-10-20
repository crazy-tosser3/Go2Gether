<script setup lang="ts">
import { ref, watch } from 'vue'
import { safetyApi, type SafetyBanner, type Sport } from '@/api'
import StoryCover from '@/components/home/StoryCover.vue'
import StoryViewer from '@/components/home/StoryViewer.vue'
import type { Story, StoryPosition } from '@/features/home/stories'
import { bannerToStory, categoryBg, categoryLabel, pickForEvent } from '@/features/safety/safety'
import { artFor } from '@/features/safety/storyArt'

const props = defineProps<{ sport: Sport }>()

const banners = ref<SafetyBanner[]>([])
const stories = ref<Story[]>([])
const position = ref<StoryPosition | null>(null)
const opening = ref<number | null>(null)

// Вспомогательный блок: при ошибке просто не показываем.
watch(
  () => props.sport.id,
  async (sportId) => {
    try {
      banners.value = pickForEvent(await safetyApi.list({ sport: sportId }), sportId)
    } catch {
      banners.value = []
    }
  },
  { immediate: true },
)

// Слайды грузим по нажатию, а листать даём между всеми памятками блока.
async function open(index: number) {
  opening.value = index
  try {
    const details = await Promise.all(banners.value.map((b) => safetyApi.get(b.id)))
    stories.value = details.map((d) => bannerToStory(d, props.sport.name))
    position.value = { story: index, slide: 0 }
  } catch {
    stories.value = []
  } finally {
    opening.value = null
  }
}
</script>

<template>
  <section
    v-if="banners.length"
    class="flex flex-col gap-3 rounded-card bg-card p-4"
    aria-labelledby="safety-title"
  >
    <h2 id="safety-title" class="font-display uppercase">{{ $t('safety.beforeTraining') }}</h2>
    <ul class="flex flex-col gap-2">
      <li v-for="(b, i) in banners" :key="b.id">
        <button
          type="button"
          class="flex w-full items-center gap-3 rounded-field p-3 text-left text-ink hover:brightness-95"
          :class="categoryBg(b.category)"
          :aria-busy="opening === i || undefined"
          @click="open(i)"
        >
          <span
            v-if="artFor(b.title)"
            aria-hidden="true"
            class="size-12 flex-none overflow-hidden rounded-md border-2 border-[#111111]"
          >
            <StoryCover :kind="artFor(b.title)!.cover" />
          </span>
          <span
            v-else
            aria-hidden="true"
            class="size-3 flex-none rounded-full ring-2 ring-card"
            :style="{ background: b.accent_color ?? 'currentColor' }"
          />
          <span class="flex min-w-0 flex-1 flex-col">
            <span class="text-xs font-bold tracking-wider uppercase opacity-80">
              {{ categoryLabel(b.category) }}
            </span>
            <b class="font-display leading-tight">{{ b.title }}</b>
            <span v-if="b.subtitle" class="truncate text-sm opacity-80">{{ b.subtitle }}</span>
          </span>
          <span aria-hidden="true" class="font-bold">→</span>
        </button>
      </li>
    </ul>
    <StoryViewer v-model:position="position" :stories="stories" />
  </section>
</template>
