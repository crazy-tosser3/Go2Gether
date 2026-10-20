<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { safetyApi, sportsApi } from '@/api'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { bannerToStory } from '@/features/safety/safety'
import { sortByArt } from '@/features/safety/storyArt'
import {
  INTRO_STORY,
  STORIES,
  readSeen,
  saveSeen,
  type Story,
  type StoryPosition,
} from '@/features/home/stories'
import StoryCover from './StoryCover.vue'
import StoryViewer from './StoryViewer.vue'

const seen = ref(readSeen())
// Порядок постоянный: сначала «Полезно знать» и памятки (от бытового к экстренному), потом сторис о сервисе.
const stories = ref<Story[]>([INTRO_STORY, ...STORIES])
const position = ref<StoryPosition | null>(null)

// Памятки с бэка; не загрузились — лента без них.
onMounted(async () => {
  try {
    const [banners, sports] = await Promise.all([safetyApi.list(), sportsApi.list()])
    const sportName = (id: number | null) => sports.find((s) => s.id === id)?.name
    const details = await Promise.allSettled(sortByArt(banners).map((b) => safetyApi.get(b.id)))
    const safety = details
      .flatMap((d) => (d.status === 'fulfilled' && d.value.slides.length ? [d.value] : []))
      .map((b) => bannerToStory(b, sportName(b.sport_id)))
    if (safety.length) stories.value = [INTRO_STORY, ...safety, ...STORIES]
  } catch {
    // без памяток лента всё равно работает
  }
})

function onSeen(id: string) {
  if (seen.value.has(id)) return
  seen.value = new Set(seen.value).add(id)
  saveSeen(seen.value)
}
</script>

<template>
  <section :aria-label="$t('stories.title')">
    <ul class="stories-row -mx-4 flex snap-x gap-4 overflow-x-auto px-4 pt-2 pb-4 lg:mx-0 lg:px-2">
      <li v-for="(s, i) in stories" :key="s.id" class="flex-none snap-start">
        <button
          type="button"
          class="relative block size-28 overflow-hidden rounded-[22px] text-left ring-3 ring-offset-3 ring-offset-page sm:size-[140px]"
          :class="[s.cover || s.intro ? '' : s.bg, seen.has(s.id) ? 'ring-mute' : 'ring-orange']"
          :aria-label="seen.has(s.id) ? s.label : $t('stories.unseen', { name: s.label })"
          @click="position = { story: i, slide: 0 }"
        >
          <span
            v-if="s.intro"
            class="absolute inset-0 flex flex-col justify-between bg-[#111111] p-3 text-[#ffffff]"
          >
            <span class="font-display text-[15px] leading-none uppercase sm:text-[17px]">
              {{ s.label }}
            </span>
            <span class="text-[11px] leading-snug font-medium text-[#e3e5ef] sm:text-[12.5px]">
              {{ s.intro.note }}
            </span>
          </span>
          <template v-else>
            <span v-if="s.cover" class="absolute inset-0"><StoryCover :kind="s.cover" /></span>
            <span
              v-else
              aria-hidden="true"
              class="pointer-events-none absolute -top-2 -right-2 opacity-70"
            >
              <SportIcon :name="s.icon" :size="72" />
            </span>
            <span
              class="absolute bottom-2 left-2 line-clamp-2 max-w-[calc(100%-16px)] hyphens-auto [overflow-wrap:anywhere] rounded-[10px] bg-[#ffffff] px-2.5 pt-1 pb-1.5 font-display text-[13px] leading-[1.1] text-[#111111] sm:text-sm"
            >
              {{ s.short ?? s.label }}
            </span>
          </template>
        </button>
      </li>
    </ul>
    <StoryViewer v-model:position="position" :stories="stories" @seen="onSeen" />
  </section>
</template>

<style scoped>
/* Фирменная прокрутка ленты из макета: тонкая дорожка, чёрный ползунок, оранжевый при наведении. */
.stories-row {
  scrollbar-width: thin;
  scrollbar-color: var(--color-ink) var(--color-mute);
}
.stories-row::-webkit-scrollbar {
  height: 8px;
}
.stories-row::-webkit-scrollbar-track {
  margin: 0 6px;
  border-radius: 999px;
  background: var(--color-mute);
}
.stories-row::-webkit-scrollbar-thumb {
  border: 2px solid var(--color-mute);
  border-radius: 999px;
  background: var(--color-ink);
}
.stories-row::-webkit-scrollbar-thumb:hover {
  background: var(--color-orange);
}
</style>
