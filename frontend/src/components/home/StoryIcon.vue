<script setup lang="ts">
import type { SlideIcon } from '@/features/safety/storyArt'

defineProps<{ name: SlideIcon }>()
</script>

<!-- Пиктограммы слайдов памяток из макета Go2Gether. -->
<template>
  <svg viewBox="0 0 100 100" class="block size-full overflow-visible" aria-hidden="true">
    <template v-if="name === 'bulb'">
      <g class="art-tw" stroke="#F28C28" stroke-width="5" stroke-linecap="round">
        <path d="M50 4 V 12 M18 18 L 24 24 M82 18 L 76 24 M8 48 H 16 M84 48 H 92" />
      </g>
      <path
        d="M50 18 Q 76 18 76 46 Q 76 60 64 70 V 78 H 36 V 70 Q 24 60 24 46 Q 24 18 50 18 Z"
        fill="#FFF7EC"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M42 50 L 50 60 L 58 50"
        fill="none"
        stroke="#F28C28"
        stroke-width="4"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <rect x="37" y="80" width="26" height="8" rx="3" fill="#111" />
      <rect x="41" y="90" width="18" height="6" rx="3" fill="#111" />
    </template>
    <template v-if="name === 'bookmark'">
      <rect
        x="14"
        y="20"
        width="56"
        height="70"
        rx="8"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M24 36 H 58 M24 50 H 58 M24 64 H 46"
        stroke="#111"
        stroke-width="4"
        stroke-linecap="round"
      />
      <g class="art-jump">
        <path
          d="M62 8 H 86 V 54 L 74 44 L 62 54 Z"
          fill="#E9B5F5"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </g>
    </template>
    <template v-if="name === 'swipe'">
      <rect x="20" y="10" width="44" height="80" rx="10" fill="#111" />
      <rect x="25" y="18" width="34" height="62" rx="4" fill="#A5B4FF" />
      <g class="art-drift">
        <path
          d="M52 64 Q 52 54 60 54 Q 66 54 66 60 V 48 Q 66 42 72 42 Q 78 42 78 48 V 70 Q 78 90 62 92 L 54 92 Q 46 88 44 80 L 40 70 Q 38 64 44 62 Q 48 62 52 68 Z"
          fill="#FFD9B8"
          stroke="#111"
          stroke-width="4"
          stroke-linejoin="round"
        />
      </g>
      <path
        d="M70 22 H 92 M84 14 L 92 22 L 84 30"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'stop'">
      <path
        d="M34 86 Q 22 74 22 58 V 40 Q 22 34 28 34 Q 34 34 34 40 V 52 V 24 Q 34 18 40 18 Q 46 18 46 24 V 48 V 18 Q 46 12 52 12 Q 58 12 58 18 V 48 V 24 Q 58 18 64 18 Q 70 18 70 24 V 62 Q 70 86 50 88 Z"
        fill="#FFD9B8"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'cold'">
      <rect
        x="16"
        y="28"
        width="68"
        height="46"
        rx="14"
        fill="#C9D3FF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M50 36 V 66 M37 43 L 63 59 M63 43 L 37 59"
        stroke="#2F3CF5"
        stroke-width="4"
        stroke-linecap="round"
      />
      <g class="art-tw" stroke="#111" stroke-width="3" stroke-linecap="round">
        <path d="M22 18 L 28 24 M78 18 L 72 24 M50 8 V 16" />
      </g>
    </template>
    <template v-if="name === 'elevate'">
      <rect
        x="10"
        y="62"
        width="50"
        height="22"
        rx="10"
        fill="#E9B5F5"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M8 88 L 40 60 L 84 40"
        fill="none"
        stroke="#111"
        stroke-width="18"
        stroke-linecap="round"
      />
      <path
        d="M8 88 L 40 60 L 84 40"
        fill="none"
        stroke="#FFD9B8"
        stroke-width="11"
        stroke-linecap="round"
      />
      <g class="art-jump">
        <path
          d="M80 26 V 8 M72 15 L 80 7 L 88 15"
          fill="none"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </g>
    </template>
    <template v-if="name === 'doctor'">
      <circle
        cx="50"
        cy="50"
        r="36"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M42 26 H 58 V 42 H 74 V 58 H 58 V 74 H 42 V 58 H 26 V 42 H 42 Z"
        fill="#E2432B"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'press'">
      <rect
        x="20"
        y="58"
        width="60"
        height="22"
        rx="6"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <g class="art-jump">
        <path
          d="M24 54 Q 24 30 40 26 H 66 Q 78 26 78 38 V 54 Z"
          fill="#FFD9B8"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M40 26 V 14 M52 26 V 12 M64 28 V 16"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
          fill="none"
        />
      </g>
      <g stroke="#111" stroke-width="3" stroke-linecap="round">
        <path d="M12 64 L 6 60 M88 64 L 94 60" />
      </g>
    </template>
    <template v-if="name === 'bandage'">
      <circle
        cx="38"
        cy="50"
        r="28"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <circle
        cx="38"
        cy="50"
        r="10"
        fill="#E9B5F5"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M38 22 H 70 Q 88 22 88 40 V 78 H 56"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path d="M60 40 H 80 M60 54 H 80" stroke="#111" stroke-width="2.5" stroke-dasharray="3 4" />
    </template>
    <template v-if="name === 'call112'">
      <rect x="26" y="8" width="48" height="84" rx="12" fill="#111" />
      <rect x="31" y="16" width="38" height="64" rx="5" fill="#FFFFFF" />
      <text
        x="50"
        y="56"
        text-anchor="middle"
        font-family="Exo 2, sans-serif"
        font-style="italic"
        font-weight="900"
        font-size="20"
        fill="#E2432B"
      >
        112
      </text>
      <g
        class="art-tw"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M80 30 Q 88 40 80 50 M86 22 Q 100 40 86 58" />
      </g>
    </template>
    <template v-if="name === 'safety'">
      <path
        d="M50 8 L 82 20 V 46 Q 82 74 50 90 Q 18 74 18 46 V 20 Z"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M34 50 L 46 62 L 68 38"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'breath'">
      <path
        d="M50 20 V 46 M50 46 Q 36 42 30 54 Q 22 72 26 84 Q 36 88 44 78 Q 50 66 50 46 Q 50 66 56 78 Q 64 88 74 84 Q 78 72 70 54 Q 64 42 50 46"
        fill="#E9B5F5"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <g class="art-tw" fill="none" stroke="#111" stroke-width="3.5" stroke-linecap="round">
        <path d="M8 30 H 30 M12 40 H 26 M70 30 H 92 M74 40 H 88" />
      </g>
    </template>
    <template v-if="name === 'cpr'">
      <path
        d="M50 84 C 18 62 12 42 22 30 C 32 18 46 22 50 34 C 54 22 68 18 78 30 C 88 42 82 62 50 84 Z"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <g class="art-jump">
        <path
          d="M30 18 H 70 V 30 H 30 Z"
          fill="#FFD9B8"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M50 30 V 46 M42 38 L 50 46 L 58 38"
          fill="none"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </g>
      <text
        x="50"
        y="72"
        text-anchor="middle"
        font-family="Exo 2, sans-serif"
        font-style="italic"
        font-weight="900"
        font-size="15"
        fill="#111"
      >
        30:2
      </text>
    </template>
    <template v-if="name === 'timer'">
      <rect x="42" y="6" width="16" height="9" rx="3" fill="#111" />
      <circle
        cx="50"
        cy="54"
        r="34"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path d="M50 24 A30 30 0 0 1 79 49 L 50 54 Z" fill="#F28C28" />
      <path
        d="M50 54 V 32 M50 54 L 64 62"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <circle cx="50" cy="54" r="4" fill="#111" />
    </template>
    <template v-if="name === 'joints'">
      <g class="art-spin">
        <path
          d="M50 14 A36 36 0 1 1 18 32"
          fill="none"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 22 L 18 34 L 30 30"
          fill="none"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </g>
      <circle
        cx="50"
        cy="50"
        r="14"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'stretch'">
      <circle cx="54" cy="16" r="8" fill="#111" />
      <path
        d="M54 26 L 50 52 M50 52 L 72 62 L 74 86 M50 52 L 30 70 L 12 74 M52 32 L 34 22 M52 32 L 72 24"
        fill="none"
        stroke="#111"
        stroke-width="7"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path d="M6 90 H 94" stroke="#111" stroke-width="4" stroke-linecap="round" />
    </template>
    <template v-if="name === 'wellbeing'">
      <path
        d="M44 82 C 16 62 10 44 18 32 C 26 20 40 24 44 34 C 48 24 62 20 70 32 C 74 38 74 46 70 54"
        fill="#F3D3FA"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M14 52 H 30 L 36 42 L 44 62 L 50 52 H 60"
        fill="none"
        stroke="#E2432B"
        stroke-width="4"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <circle
        cx="74"
        cy="72"
        r="16"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M74 64 V 74 M74 80 V 81"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'reflect'">
      <path
        d="M30 20 L 40 12 H 60 L 70 20 L 80 34 L 72 40 V 88 H 28 V 40 L 20 34 Z"
        fill="#F28C28"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path d="M28 54 H 72 M28 68 H 72" stroke="#DDF46B" stroke-width="7" />
      <g class="art-tw" stroke="#DDF46B" stroke-width="3.5" stroke-linecap="round">
        <path d="M10 50 H 2 M90 50 H 98 M12 64 L 4 68 M88 64 L 96 68" />
      </g>
    </template>
    <template v-if="name === 'road'">
      <path d="M30 96 L 42 4 H 58 L 70 96 Z" fill="#111" />
      <path
        class="art-flow"
        d="M50 8 V 96"
        stroke="#FFFFFF"
        stroke-width="3"
        stroke-dasharray="8 8"
      />
      <path d="M20 96 L 32 4" stroke="#F28C28" stroke-width="5" />
      <g transform="translate(16 52)">
        <circle r="7" fill="#A5B4FF" stroke="#111" stroke-width="3" />
      </g>
      <path
        d="M84 70 V 30 M76 38 L 84 30 L 92 38"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'headphones'">
      <path
        d="M20 60 V 50 Q 20 18 50 18 Q 80 18 80 50 V 60"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <rect
        x="12"
        y="54"
        width="18"
        height="30"
        rx="8"
        fill="#E9B5F5"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <rect
        x="70"
        y="54"
        width="18"
        height="30"
        rx="8"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="4"
        stroke-dasharray="5 4"
      />
      <path d="M40 58 L 46 52 V 82 L 40 76 H 34 V 58 Z" fill="#111" />
      <path
        d="M52 60 Q 56 67 52 74"
        fill="none"
        stroke="#111"
        stroke-width="3"
        stroke-linecap="round"
      />
    </template>
    <template v-if="name === 'route'">
      <path
        class="art-flow"
        d="M18 80 C 30 50 70 76 74 40"
        fill="none"
        stroke="#111"
        stroke-width="4"
        stroke-dasharray="7 7"
        stroke-linecap="round"
      />
      <circle cx="18" cy="80" r="7" fill="#A5B4FF" stroke="#111" stroke-width="4" />
      <g class="art-jump">
        <path
          d="M74 46 C 60 32 60 12 74 10 C 88 12 88 32 74 46 Z"
          fill="#F28C28"
          stroke="#111"
          stroke-width="5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="74" cy="25" r="5" fill="#111" />
      </g>
    </template>
    <template v-if="name === 'water'">
      <rect x="38" y="6" width="24" height="12" rx="3" fill="#111" />
      <path
        d="M34 26 Q 34 18 42 18 H 58 Q 66 18 66 26 V 86 Q 66 94 58 94 H 42 Q 34 94 34 86 Z"
        fill="#C9D3FF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path d="M36 48 H 64 V 86 Q 64 92 58 92 H 42 Q 36 92 36 86 Z" fill="#A5B4FF" />
      <path d="M44 56 V 80" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round" />
    </template>
    <template v-if="name === 'kit'">
      <path
        d="M38 26 V 18 Q 38 12 44 12 H 56 Q 62 12 62 18 V 26"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <rect
        x="12"
        y="26"
        width="76"
        height="58"
        rx="12"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M44 40 H 56 V 50 H 66 V 62 H 56 V 72 H 44 V 62 H 34 V 50 H 44 Z"
        fill="#E2432B"
        stroke="#111"
        stroke-width="3"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'phone'">
      <rect x="14" y="10" width="40" height="78" rx="10" fill="#111" />
      <rect x="19" y="18" width="30" height="60" rx="4" fill="#FFFFFF" />
      <rect
        x="26"
        y="34"
        width="16"
        height="30"
        rx="3"
        fill="none"
        stroke="#111"
        stroke-width="3"
      />
      <rect x="30" y="44" width="8" height="17" fill="#8FD39A" />
      <rect
        x="60"
        y="40"
        width="28"
        height="44"
        rx="7"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        class="art-tw"
        d="M76 50 L 70 62 H 78 L 72 74"
        fill="none"
        stroke="#111"
        stroke-width="3"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path d="M54 58 Q 58 58 60 62" fill="none" stroke="#111" stroke-width="3" />
    </template>
    <template v-if="name === 'compass'">
      <circle
        cx="44"
        cy="52"
        r="34"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <g class="art-spin">
        <path
          d="M44 24 L 52 52 L 44 80 L 36 52 Z"
          fill="#E2432B"
          stroke="#111"
          stroke-width="3"
          stroke-linejoin="round"
        />
        <path
          d="M44 52 L 52 52 L 44 80 L 36 52 Z"
          fill="#A5B4FF"
          stroke="#111"
          stroke-width="3"
          stroke-linejoin="round"
        />
      </g>
      <circle cx="44" cy="52" r="4" fill="#111" />
      <path
        d="M70 14 H 92 V 28 H 80 L 76 34 L 70 28 Z"
        fill="#F28C28"
        stroke="#111"
        stroke-width="3.5"
        stroke-linejoin="round"
      />
    </template>
    <template v-if="name === 'numbers'">
      <rect
        x="14"
        y="10"
        width="72"
        height="80"
        rx="14"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <g
        font-family="Exo 2, sans-serif"
        font-style="italic"
        font-weight="900"
        font-size="15"
        text-anchor="middle"
      >
        <text x="50" y="32" fill="#E2432B" font-size="20">112</text>
        <text x="34" y="56" fill="#111">103</text>
        <text x="66" y="56" fill="#111">101</text>
        <text x="50" y="78" fill="#111">102</text>
      </g>
    </template>
    <template v-if="name === 'speech'">
      <path
        d="M12 16 H 88 Q 92 16 92 20 V 62 Q 92 66 88 66 H 40 L 24 82 V 66 H 12 Q 8 66 8 62 V 20 Q 8 16 12 16 Z"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M22 32 H 70 M22 44 H 78 M22 56 H 56"
        stroke="#111"
        stroke-width="4"
        stroke-linecap="round"
      />
      <circle cx="78" cy="56" r="4" fill="#E2432B" />
    </template>
    <template v-if="name === 'hold'">
      <g transform="rotate(-20 50 50)">
        <path
          d="M22 40 Q 22 24 36 24 L 44 26 L 46 40 L 38 44 Q 42 58 56 64 L 60 56 L 74 58 L 76 68 Q 76 82 60 82 Q 22 76 22 40 Z"
          fill="#111"
        />
      </g>
      <g class="art-tw">
        <circle cx="64" cy="24" r="4" fill="#111" />
        <circle cx="76" cy="24" r="4" fill="#111" />
        <circle cx="88" cy="24" r="4" fill="#111" />
      </g>
    </template>
  </svg>
</template>
