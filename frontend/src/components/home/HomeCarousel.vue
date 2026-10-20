<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { activePromos, promoPeriod } from '@/features/home/promos'
import HomeBannerArt from './HomeBannerArt.vue'

const promos = activePromos(new Date())
const track = ref<HTMLElement | null>(null)

// Первая карточка — приветствие, дальше акции. Листаем на ширину одного баннера; автопрокрутки нет — текст успевают прочитать.
function scrollBy(direction: 1 | -1) {
  const el = track.value
  const card = el?.firstElementChild as HTMLElement | null
  if (!el || !card) return
  el.scrollBy({ left: direction * (card.offsetWidth + 14), behavior: 'smooth' })
}
</script>

<template>
  <section class="relative" :aria-label="$t('home.main')">
    <ul
      ref="track"
      class="-mx-4 flex snap-x snap-mandatory scroll-px-4 gap-3.5 overflow-x-auto px-4 pb-1 [scrollbar-width:none] lg:mx-0 lg:scroll-px-0 lg:px-0"
    >
      <li
        class="relative flex min-h-[300px] w-[86%] flex-none snap-start flex-col justify-between gap-[18px] overflow-hidden rounded-[30px] bg-accent p-[22px] sm:min-h-[280px] sm:w-[calc(53%-7px)] sm:px-8 sm:py-[30px]"
      >
        <span
          aria-hidden="true"
          class="pointer-events-none absolute -right-2.5 -bottom-1.5 h-[62%] w-[88%] sm:h-full sm:w-[58%]"
        >
          <HomeBannerArt kind="runners" />
        </span>
        <div class="relative flex max-w-[360px] flex-col gap-2.5">
          <p
            class="-rotate-3 self-start rounded-[10px] bg-[#fff7ec] px-3 pt-0.5 pb-1 font-hand text-[22px] text-[#111111]"
          >
            {{ $t('home.slogan') }}
          </p>
          <h1 class="text-[28px] leading-[0.98] sm:text-[40px]">{{ $t('home.title') }}</h1>
        </div>
        <RouterLink
          :to="{ name: 'events' }"
          class="relative inline-flex min-h-[50px] w-fit items-center rounded-full bg-ink px-6 font-bold whitespace-nowrap text-white no-underline hover:bg-ink/85"
        >
          {{ $t('home.cta') }}
        </RouterLink>
      </li>
      <li
        v-for="p in promos"
        :key="p.id"
        class="relative flex min-h-[300px] w-[86%] flex-none snap-start flex-col justify-between gap-[18px] overflow-hidden rounded-[30px] p-[22px] sm:min-h-[280px] sm:w-[calc(47%-7px)] sm:px-8 sm:py-[30px]"
        :class="p.bg"
      >
        <span
          v-if="p.art"
          aria-hidden="true"
          class="pointer-events-none absolute -right-10 -bottom-5 h-[55%] w-[70%] opacity-95 sm:h-[78%] sm:w-[52%]"
        >
          <HomeBannerArt :kind="p.art" />
        </span>
        <span
          v-else
          aria-hidden="true"
          class="pointer-events-none absolute -right-3 -bottom-3 opacity-80"
        >
          <SportIcon :name="p.icon" :size="110" />
        </span>
        <div class="relative flex max-w-[330px] flex-col gap-2.5">
          <p class="flex flex-wrap items-center gap-2.5 text-sm font-bold">
            <span class="rounded-full bg-ink px-3 py-1 text-[13px] text-white">{{ p.tag }}</span>
            <span>{{ promoPeriod(p) }}</span>
          </p>
          <h2 class="font-display text-[26px] leading-[0.98] uppercase sm:text-[34px]">
            {{ p.title }}
          </h2>
          <p class="text-[15px] leading-normal">{{ p.text }}</p>
        </div>
        <RouterLink
          :to="p.to"
          class="relative inline-flex min-h-[50px] w-fit items-center rounded-full bg-ink px-6 font-bold whitespace-nowrap text-white no-underline hover:bg-ink/85"
        >
          {{ p.cta }}
        </RouterLink>
      </li>
    </ul>
    <template v-if="promos.length">
      <button
        v-for="d in [-1, 1] as const"
        :key="d"
        type="button"
        class="absolute top-1/2 hidden size-12 -translate-y-1/2 place-items-center rounded-full bg-card shadow-[0_4px_14px_rgb(0_0_0/0.18)] hover:bg-field sm:grid"
        :class="d === -1 ? '-left-4' : '-right-4'"
        :aria-label="d === -1 ? $t('common.back') : $t('common.next')"
        @click="scrollBy(d)"
      >
        <svg
          width="18"
          height="18"
          viewBox="0 0 18 18"
          aria-hidden="true"
          fill="none"
          stroke="currentColor"
          stroke-width="2.2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path :d="d === -1 ? 'M15 9H3M8 4L3 9l5 5' : 'M3 9h12M10 4l5 5-5 5'" />
        </svg>
      </button>
    </template>
  </section>
</template>
