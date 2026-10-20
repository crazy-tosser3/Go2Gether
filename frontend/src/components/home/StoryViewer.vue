<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { SLIDE_MS, stepStory, type Story, type StoryPosition } from '@/features/home/stories'
import StoryCover from './StoryCover.vue'
import StoryIcon from './StoryIcon.vue'

const props = defineProps<{ stories: Story[] }>()
const position = defineModel<StoryPosition | null>('position', { required: true })
const emit = defineEmits<{ seen: [id: string] }>()

const dialog = ref<HTMLDialogElement>()
const elapsed = ref(0)
const paused = ref(false)
// Автоперелистывание — движение без спроса: при «меньше движения» стартуем на паузе (ГОСТ Р 52872, 2.2.2).
const reduceMotion =
  typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches

const story = computed(() => (position.value ? props.stories[position.value.story] : undefined))
const slide = computed(() => story.value?.slides[position.value?.slide ?? 0])
const isLastSlide = computed(
  () => !!story.value && (position.value?.slide ?? 0) === story.value.slides.length - 1,
)
const hasNextStory = computed(() => (position.value?.story ?? 0) < props.stories.length - 1)

// Цвета макета — парами «фон и текст»; у сторис без них фон из класса карточки, текст — цвета темы.
const colors = computed(() => {
  const p = story.value?.palette
  return p
    ? { bg: p.bg, fg: p.fg, inv: p.bg }
    : { bg: undefined, fg: 'var(--color-ink)', inv: 'var(--color-page)' }
})

watch(position, async (p) => {
  await nextTick()
  const el = dialog.value
  if (!el) return
  if (p && story.value) {
    if (!el.open) {
      el.showModal?.()
      paused.value = reduceMotion
    }
    elapsed.value = 0
    emit('seen', story.value.id)
  } else if (el.open) {
    el.close()
  }
})

function go(direction: 1 | -1) {
  if (!position.value) return
  position.value = stepStory(props.stories, position.value, direction)
}

let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  timer = setInterval(() => {
    if (!position.value || paused.value) return
    elapsed.value += 100
    if (elapsed.value >= SLIDE_MS) go(1)
  }, 100)
})
onUnmounted(() => clearInterval(timer))

function onKey(e: KeyboardEvent) {
  if (e.key === 'ArrowRight') go(1)
  else if (e.key === 'ArrowLeft') go(-1)
}

function barWidth(i: number) {
  const at = position.value?.slide ?? 0
  if (i < at) return '100%'
  if (i > at) return '0%'
  return `${Math.min(100, (elapsed.value / SLIDE_MS) * 100)}%`
}
</script>

<template>
  <dialog
    ref="dialog"
    :aria-label="story ? $t('stories.viewer', { name: story.label }) : $t('stories.title')"
    class="m-auto h-dvh max-h-none w-full max-w-none bg-transparent p-0 backdrop:bg-black/80 sm:h-[min(800px,94dvh)] sm:w-[420px]"
    @close="position = null"
    @keydown="onKey"
  >
    <section
      v-if="story && slide && position"
      class="flex h-full flex-col gap-3.5 overflow-hidden px-[18px] pt-3.5 pb-[18px] sm:rounded-2xl"
      :class="story.palette ? '' : story.bg"
      :style="{ backgroundColor: colors.bg, color: colors.fg }"
    >
      <div class="flex gap-1" aria-hidden="true">
        <span
          v-for="(_, i) in story.slides"
          :key="i"
          class="relative h-1 flex-1 overflow-hidden rounded-full"
        >
          <span class="absolute inset-0 bg-current opacity-25" />
          <span class="absolute inset-y-0 left-0 bg-current" :style="{ width: barWidth(i) }" />
        </span>
      </div>

      <header class="flex items-center gap-2.5">
        <span
          class="grid size-[42px] flex-none place-items-center overflow-hidden rounded-full border-2 border-[#111111] bg-[#ffffff]"
          aria-hidden="true"
        >
          <StoryCover v-if="story.cover" :kind="story.cover" />
          <SportIcon v-else :name="story.icon" :size="30" />
        </span>
        <span class="flex min-w-0 flex-1 flex-col gap-0.5">
          <span class="truncate font-display text-base leading-tight">{{ story.label }}</span>
          <span class="flex items-center gap-1.5 text-xs font-semibold">
            <span
              v-if="story.badgeColor"
              aria-hidden="true"
              class="size-2 flex-none rounded-full shadow-[0_0_0_1.5px_#ffffff]"
              :style="{ background: story.badgeColor }"
            />
            <span class="truncate">
              <template v-if="story.badge">{{ story.badge }} · </template>
              {{ $t('stories.position', { n: position.slide + 1, total: story.slides.length }) }}
            </span>
          </span>
        </span>
        <button
          type="button"
          class="grid size-11 flex-none place-items-center rounded-full bg-[#ffffff]/75 font-bold text-[#111111] hover:bg-[#ffffff]"
          :aria-label="paused ? $t('stories.resume') : $t('stories.pause')"
          @click="paused = !paused"
        >
          {{ paused ? '▶' : '❚❚' }}
        </button>
        <button
          type="button"
          class="grid size-11 flex-none place-items-center rounded-full bg-[#ffffff]/75 text-[#111111] hover:bg-[#ffffff]"
          :aria-label="$t('common.close')"
          @click="position = null"
        >
          <svg
            width="16"
            height="16"
            viewBox="0 0 16 16"
            aria-hidden="true"
            fill="none"
            stroke="currentColor"
            stroke-width="2.4"
            stroke-linecap="round"
          >
            <path d="M3 3L13 13M13 3L3 13" />
          </svg>
        </button>
      </header>

      <div
        class="flex min-h-0 flex-1 flex-col justify-center gap-3.5 overflow-y-auto"
        role="group"
        aria-live="polite"
        :aria-label="$t('stories.slide', { n: position.slide + 1, total: story.slides.length })"
      >
        <div
          class="relative grid aspect-square w-[min(62vw,230px,30vh)] flex-none place-items-center self-center short:hidden"
          aria-hidden="true"
        >
          <svg viewBox="0 0 200 200" class="absolute inset-0 size-full">
            <polygon
              class="art-zoom"
              points="100,0 116,52 170,30 148,84 200,100 148,116 170,170 116,148 100,200 84,148 30,170 52,116 0,100 52,84 30,30 84,52"
              fill="#ffffff"
              fill-opacity="0.55"
            />
          </svg>
          <span
            class="relative grid aspect-square w-[70%] place-items-center rounded-full border-[5px] border-[#111111] bg-[#ffffff] p-[14%]"
          >
            <StoryIcon v-if="slide.icon" :name="slide.icon" />
            <SportIcon v-else :name="story.icon" :size="120" />
          </span>
        </div>

        <p
          v-if="story.subtitle"
          class="-rotate-2 self-start rounded-sm bg-[#fff7ec] px-3 pt-0.5 pb-1 font-hand text-[21px] text-[#111111]"
        >
          {{ story.subtitle }}
        </p>
        <h2 class="font-display text-[26px] leading-none uppercase sm:text-[30px]">
          {{ slide.title }}
        </h2>
        <p class="text-lg leading-snug font-medium">{{ slide.text }}</p>
        <a
          v-if="slide.call"
          href="tel:112"
          class="inline-flex min-h-[50px] items-center gap-2.5 self-start rounded-full bg-[#111111] px-[22px] font-extrabold text-[#ffffff] no-underline shadow-[0_0_0_2px_#ffffff]"
        >
          <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true">
            <path
              d="M3 4Q3 2 5 2L7 2.5L7.5 6L6 7Q7 10 11 12L12 10.5L15.5 11L16 13Q16 16 13 16Q3 15 3 4Z"
              fill="currentColor"
            />
          </svg>
          {{ $t('stories.call112') }}
        </a>
        <RouterLink
          v-if="slide.cta"
          :to="slide.cta.to"
          class="inline-flex min-h-13 w-fit items-center rounded-full bg-ink px-7 font-bold text-white no-underline hover:bg-ink/85"
          @click="position = null"
        >
          {{ slide.cta.label }}
        </RouterLink>
      </div>

      <p v-if="story.disclaimer" class="text-xs leading-snug font-medium">
        {{ story.disclaimer }}
      </p>

      <div class="flex gap-2.5">
        <button
          type="button"
          class="min-h-[52px] flex-1 rounded-full border-2 border-current font-bold disabled:opacity-40"
          :disabled="position.story === 0 && position.slide === 0"
          @click="go(-1)"
        >
          {{ $t('stories.back') }}
        </button>
        <button
          type="button"
          class="min-h-[52px] flex-[1.4] rounded-full font-extrabold"
          :style="{ backgroundColor: colors.fg, color: colors.inv }"
          @click="isLastSlide && !hasNextStory ? (position = null) : go(1)"
        >
          {{
            !isLastSlide
              ? $t('stories.forward')
              : hasNextStory
                ? $t('stories.nextTopic')
                : $t('common.close')
          }}
        </button>
      </div>
    </section>
  </dialog>
</template>
