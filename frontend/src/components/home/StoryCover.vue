<script setup lang="ts">
import { useId } from 'vue'
import type { CoverKind } from '@/features/safety/storyArt'

defineProps<{ kind: CoverKind }>()
const uid = useId()
</script>

<!-- Обложки памяток из макета Go2Gether: плоская графика, полутоновые точки, лёгкая анимация. -->
<template>
  <svg
    viewBox="0 0 160 160"
    preserveAspectRatio="xMidYMid slice"
    class="block size-full"
    aria-hidden="true"
  >
    <template v-if="kind === 'bruise'">
      <defs>
        <pattern :id="`sc0-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.22" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#C9D3FF" />
      <rect width="160" height="160" :fill="`url(#sc0-${uid})`" />
      <circle cx="112" cy="58" r="52" fill="#A5B4FF" />
      <path
        d="M30 170 L 92 96 L 176 112"
        fill="none"
        stroke="#111"
        stroke-width="40"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M30 170 L 92 96 L 176 112"
        fill="none"
        stroke="#F2C4A0"
        stroke-width="31"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <g class="art-zoom">
        <g transform="rotate(-18 96 82)">
          <rect
            x="68"
            y="62"
            width="58"
            height="38"
            rx="12"
            fill="#FFFFFF"
            stroke="#111"
            stroke-width="4"
          />
          <path
            d="M97 70 V 92 M87 75 L 107 87 M107 75 L 87 87"
            stroke="#2F3CF5"
            stroke-width="3.2"
            stroke-linecap="round"
          />
        </g>
      </g>
      <g class="art-tw" fill="none" stroke="#FFFFFF" stroke-width="3.5" stroke-linecap="round">
        <path d="M128 40 L 138 30 M134 52 H 148 M116 30 V 18" />
      </g>
    </template>
    <template v-if="kind === 'bleeding'">
      <defs>
        <pattern :id="`sc1-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.22" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#E9B5F5" />
      <rect width="160" height="160" :fill="`url(#sc1-${uid})`" />
      <circle cx="110" cy="60" r="54" fill="#F3D3FA" />
      <g class="art-drift" transform="translate(126 112)">
        <circle r="26" fill="#FFFFFF" stroke="#111" stroke-width="4" />
        <circle r="10" fill="#E9B5F5" stroke="#111" stroke-width="3" />
        <path d="M0 -26 Q 34 -24 40 10 L 34 30" fill="none" stroke="#111" stroke-width="4" />
      </g>
      <g class="art-zoom">
        <g transform="rotate(-35 92 66)">
          <rect
            x="30"
            y="44"
            width="124"
            height="44"
            rx="22"
            fill="#FFD9B8"
            stroke="#111"
            stroke-width="4"
          />
          <rect
            x="72"
            y="50"
            width="40"
            height="32"
            rx="6"
            fill="#FFFFFF"
            stroke="#111"
            stroke-width="3"
          />
          <g fill="#111" fill-opacity="0.35">
            <circle cx="44" cy="60" r="2" />
            <circle cx="52" cy="72" r="2" />
            <circle cx="140" cy="60" r="2" />
            <circle cx="132" cy="72" r="2" />
          </g>
          <path
            d="M92 58 V 74 M84 66 H 100"
            stroke="#E2432B"
            stroke-width="4"
            stroke-linecap="round"
          />
        </g>
      </g>
    </template>
    <template v-if="kind === 'cpr'">
      <defs>
        <pattern :id="`sc2-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.24" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#F28C28" />
      <rect width="160" height="160" :fill="`url(#sc2-${uid})`" />
      <g class="art-zoom">
        <path
          d="M106 116 C 60 84 54 58 66 42 C 78 26 100 30 106 48 C 112 30 134 26 146 42 C 158 58 152 84 106 116 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="5"
          stroke-linejoin="round"
        />
      </g>
      <path
        class="art-flow"
        d="M-4 76 H 60 L 72 54 L 86 100 L 98 64 L 106 76 H 170"
        fill="none"
        stroke="#111"
        stroke-width="5"
        stroke-linejoin="round"
        stroke-linecap="round"
        stroke-dasharray="180 40"
      />
      <g class="art-tw" fill="#FFFFFF">
        <path d="M40 30 h6 v-6 h4 v6 h6 v4 h-6 v6 h-4 v-6 h-6 Z" />
      </g>
    </template>
    <template v-if="kind === 'warmup'">
      <defs>
        <pattern :id="`sc3-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.22" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#A5B4FF" />
      <rect width="160" height="160" :fill="`url(#sc3-${uid})`" />
      <g class="art-spin">
        <circle
          cx="106"
          cy="70"
          r="56"
          fill="none"
          stroke="#111"
          stroke-width="4"
          stroke-dasharray="26 14"
        />
        <path d="M160 66 L 164 80 L 150 78 Z" fill="#111" />
      </g>
      <rect x="98" y="16" width="16" height="10" rx="3" fill="#111" />
      <circle cx="106" cy="70" r="40" fill="#FFFFFF" stroke="#111" stroke-width="5" />
      <path
        d="M106 70 V 44 M106 70 L 122 80"
        stroke="#111"
        stroke-width="5"
        stroke-linecap="round"
      />
      <path d="M106 34 A36 36 0 0 1 141 64 L 106 70 Z" fill="#F28C28" fill-opacity="0.85" />
      <circle cx="106" cy="70" r="5" fill="#111" />
    </template>
    <template v-if="kind === 'run'">
      <defs>
        <pattern :id="`sc4-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#FFFFFF" fill-opacity="0.12" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#111" />
      <rect width="160" height="160" :fill="`url(#sc4-${uid})`" />
      <path d="M136 24 A22 22 0 1 0 150 62 A18 18 0 1 1 136 24 Z" fill="#FFF7EC" />
      <g class="art-tw" fill="#FFF7EC">
        <circle cx="96" cy="22" r="2" />
        <circle cx="118" cy="70" r="1.6" />
      </g>
      <path
        class="art-flow"
        d="M-10 140 H 170"
        stroke="#FFF7EC"
        stroke-width="4"
        stroke-dasharray="20 14"
      />
      <g class="art-drift">
        <g transform="translate(56 82)">
          <path
            d="M0 34 L 6 4 Q 8 -6 20 -4 L 44 2 Q 56 6 62 16 L 84 22 Q 98 26 96 38 L 0 38 Z"
            fill="#F28C28"
            stroke="#FFF7EC"
            stroke-width="3"
            stroke-linejoin="round"
          />
          <rect x="-2" y="36" width="100" height="8" rx="4" fill="#FFF7EC" />
          <path
            d="M30 6 L 38 26 M44 10 L 52 28"
            stroke="#DDF46B"
            stroke-width="5"
            stroke-linecap="round"
          />
          <g class="art-tw2" stroke="#DDF46B" stroke-width="3" stroke-linecap="round">
            <path d="M28 -12 L 24 -22 M40 -10 L 42 -22 M52 -6 L 60 -16" />
          </g>
        </g>
      </g>
    </template>
    <template v-if="kind === 'pack'">
      <defs>
        <pattern :id="`sc5-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.16" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#FFFFFF" />
      <rect width="160" height="160" :fill="`url(#sc5-${uid})`" />
      <circle cx="104" cy="64" r="52" fill="#C9D3FF" />
      <g class="art-zoom">
        <path
          d="M88 22 Q 88 10 100 10 H 112 Q 124 10 124 22"
          fill="none"
          stroke="#111"
          stroke-width="5"
        />
        <rect
          x="72"
          y="20"
          width="68"
          height="88"
          rx="22"
          fill="#A5B4FF"
          stroke="#111"
          stroke-width="5"
        />
        <rect
          x="82"
          y="64"
          width="48"
          height="34"
          rx="10"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="4"
        />
        <path
          d="M106 72 V 90 M97 81 H 115"
          stroke="#E2432B"
          stroke-width="5"
          stroke-linecap="round"
        />
      </g>
      <g class="art-drift">
        <rect
          x="40"
          y="56"
          width="26"
          height="56"
          rx="10"
          fill="#E9B5F5"
          stroke="#111"
          stroke-width="4"
        />
        <rect x="45" y="46" width="16" height="12" rx="3" fill="#111" />
        <path d="M44 78 H 62" stroke="#FFFFFF" stroke-width="4" />
      </g>
    </template>
    <template v-if="kind === 'emergency'">
      <defs>
        <pattern :id="`sc6-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.22" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#E2432B" />
      <rect width="160" height="160" :fill="`url(#sc6-${uid})`" />
      <text
        x="152"
        y="60"
        text-anchor="end"
        font-family="Exo 2, sans-serif"
        font-style="italic"
        font-weight="900"
        font-size="54"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="3"
        paint-order="stroke"
      >
        112
      </text>
      <g class="art-zoom">
        <g transform="rotate(-30 100 100)">
          <path
            d="M70 92 Q 70 76 84 76 L 94 78 L 96 92 L 88 96 Q 92 110 108 116 L 112 108 L 126 110 L 128 120 Q 128 134 112 134 Q 70 128 70 92 Z"
            fill="#111"
          />
        </g>
      </g>
      <g class="art-tw" fill="none" stroke="#111" stroke-width="4" stroke-linecap="round">
        <path d="M128 82 Q 138 92 132 104 M138 74 Q 154 92 144 112" />
      </g>
    </template>
    <template v-if="kind === 'intro'">
      <defs>
        <pattern :id="`sc7-${uid}`" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.4" fill="#111" fill-opacity="0.18" />
        </pattern>
      </defs>
      <rect width="160" height="160" fill="#FFF7EC" />
      <rect width="160" height="160" :fill="`url(#sc7-${uid})`" />
      <g transform="translate(108 58)">
        <polygon
          class="art-zoom"
          points="0,-58 12,-24 48,-36 30,-6 58,0 30,8 48,38 12,26 0,58 -12,26 -48,38 -30,8 -58,0 -30,-6 -48,-36 -12,-24"
          fill="#F28C28"
        />
      </g>
      <g class="art-jump">
        <path
          d="M108 22 Q 134 22 134 50 Q 134 64 122 74 V 84 H 94 V 74 Q 82 64 82 50 Q 82 22 108 22 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="5"
          stroke-linejoin="round"
        />
        <path
          d="M100 54 L 108 64 L 116 54"
          fill="none"
          stroke="#F28C28"
          stroke-width="4"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="95" y="86" width="26" height="8" rx="3" fill="#111" />
      </g>
      <path
        d="M18 -2 H 44 V 52 L 31 42 L 18 52 Z"
        fill="#E9B5F5"
        stroke="#111"
        stroke-width="4"
        stroke-linejoin="round"
      />
    </template>
  </svg>
</template>
