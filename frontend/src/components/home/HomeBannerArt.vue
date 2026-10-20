<script setup lang="ts">
import { useId } from 'vue'

defineProps<{ kind: 'runners' | 'kick' }>()
const uid = useId()
</script>

<!-- Иллюстрации баннеров главной из макета Go2Gether. -->
<template>
  <svg
    v-if="kind === 'runners'"
    viewBox="0 0 580 420"
    preserveAspectRatio="xMaxYMax meet"
    class="block size-full"
    aria-hidden="true"
  >
    <defs>
      <pattern :id="`htW-${uid}`" width="10" height="10" patternUnits="userSpaceOnUse">
        <circle cx="5" cy="5" r="2" fill="#FFFFFF" fill-opacity="0.45" />
      </pattern>
      <pattern :id="`htD-${uid}`" width="9" height="9" patternUnits="userSpaceOnUse">
        <circle cx="4.5" cy="4.5" r="1.6" fill="#111" fill-opacity="0.35" />
      </pattern>
    </defs>
    <path d="M0 0 H580 V210 Q 290 150 0 230 Z" :fill="`url(#htD-${uid})`" />
    <circle cx="395" cy="185" r="160" fill="#F28C28" />
    <path d="M395 25 A160 160 0 0 1 555 185 A160 160 0 0 1 395 345 Z" :fill="`url(#htW-${uid})`" />
    <path d="M-20 300 Q 290 230 600 280 L600 420 L-20 420 Z" fill="#111" />
    <path d="M-20 300 Q 290 230 600 280" fill="none" stroke="#FFFFFF" stroke-width="4" />
    <path
      class="art-flow"
      d="M-20 345 Q 290 275 600 325"
      fill="none"
      stroke="#A5B4FF"
      stroke-width="4"
      stroke-dasharray="40 24"
    />
    <path
      class="art-flow"
      d="M-20 390 Q 290 320 600 370"
      fill="none"
      stroke="#E9B5F5"
      stroke-width="4"
      stroke-dasharray="40 24"
    />
    <path class="art-st" d="M40 150 L190 144 Q196 150 190 156 Z" fill="#FFF7EC" />
    <path class="art-st2" d="M20 205 L170 200 Q175 205 170 210 Z" fill="#FFF7EC" />
    <path class="art-st3" d="M70 255 L200 251 Q205 255 200 259 Z" fill="#FFFFFF" />
    <path class="art-st2" d="M110 110 L220 106 Q224 110 220 114 Z" fill="#E9B5F5" />
    <g transform="translate(232 112) scale(0.82)">
      <g class="art-run2">
        <g fill="none" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 52 L-30 84 L-58 78" stroke="#111" stroke-width="18" />
          <path d="M0 128 L-44 176 L-96 168" stroke="#111" stroke-width="24" />
          <path d="M10 46 L2 118" stroke="#FFFFFF" stroke-width="46" />
          <path d="M2 114 L0 136" stroke="#F28C28" stroke-width="50" />
          <path d="M4 132 L52 168 L36 236" stroke="#111" stroke-width="24" />
          <path d="M10 54 L46 86 L76 58" stroke="#111" stroke-width="18" />
        </g>
        <ellipse cx="-102" cy="170" rx="18" ry="9" fill="#FFFFFF" />
        <ellipse cx="46" cy="240" rx="20" ry="9" fill="#FFFFFF" />
        <circle cx="18" cy="8" r="23" fill="#111" />
        <path d="M-2 0 Q-30 -6 -44 14 Q-26 8 -8 14 Z" fill="#111" />
      </g>
    </g>
    <g transform="translate(372 70)">
      <g class="art-run">
        <g fill="none" stroke-linecap="round" stroke-linejoin="round">
          <path d="M4 52 L-30 84 L-58 78" stroke="#111" stroke-width="18" />
          <path d="M0 128 L-44 176 L-96 168" stroke="#111" stroke-width="24" />
          <path d="M10 46 L2 118" stroke="#1B1F6B" stroke-width="46" />
          <path d="M10 50 L6 100" stroke="#A5B4FF" stroke-width="8" />
          <path d="M2 114 L0 136" stroke="#E9B5F5" stroke-width="50" />
          <path d="M4 132 L52 168 L36 236" stroke="#111" stroke-width="24" />
          <path d="M10 54 L46 86 L76 58" stroke="#111" stroke-width="18" />
        </g>
        <ellipse cx="-102" cy="170" rx="18" ry="9" fill="#F28C28" />
        <ellipse cx="46" cy="240" rx="20" ry="9" fill="#F28C28" />
        <circle cx="18" cy="8" r="23" fill="#111" />
      </g>
    </g>
    <g class="art-tw" transform="translate(520 60)">
      <path
        d="M0 -14 L3.5 -3.5 L14 0 L3.5 3.5 L0 14 L-3.5 3.5 L-14 0 L-3.5 -3.5 Z"
        fill="#FFFFFF"
      />
    </g>
    <g class="art-tw2" transform="translate(250 50)">
      <path d="M0 -10 L2.5 -2.5 L10 0 L2.5 2.5 L0 10 L-2.5 2.5 L-10 0 L-2.5 -2.5 Z" fill="#111" />
    </g>
  </svg>
  <svg
    v-else
    viewBox="0 0 460 330"
    preserveAspectRatio="xMaxYMax meet"
    class="block size-full"
    aria-hidden="true"
  >
    <defs>
      <pattern :id="`htI-${uid}`" width="9" height="9" patternUnits="userSpaceOnUse">
        <circle cx="4.5" cy="4.5" r="1.7" fill="#111" fill-opacity="0.4" />
      </pattern>
      <clipPath :id="`ballI-${uid}`"><circle cx="0" cy="0" r="90" /></clipPath>
    </defs>
    <path d="M0 0 H460 V140 Q 230 100 0 160 Z" :fill="`url(#htI-${uid})`" />
    <path d="M0 150 Q 230 95 460 135" fill="none" stroke="#111" stroke-width="6" />
    <path
      class="art-flow"
      d="M0 175 Q 230 125 460 160"
      fill="none"
      stroke="#FFF7EC"
      stroke-width="3"
      stroke-dasharray="30 18"
    />
    <path d="M0 250 Q 230 195 460 240 V330 H0 Z" fill="#111" />
    <path
      class="art-flow"
      d="M0 285 Q 230 232 460 275"
      fill="none"
      stroke="#A5B4FF"
      stroke-width="3"
      stroke-dasharray="40 22"
    />
    <g transform="rotate(-28 230 165)">
      <path class="art-st" d="M120 150 L260 145 Q266 150 260 155 Z" fill="#FFF7EC" />
      <path class="art-st2" d="M100 178 L240 173 Q246 178 240 183 Z" fill="#FFFFFF" />
      <path class="art-st3" d="M140 205 L250 201 Q255 205 250 209 Z" fill="#E9B5F5" />
    </g>
    <g transform="translate(335 110)">
      <g class="art-zoom">
        <g class="art-spin">
          <circle cx="0" cy="0" r="90" fill="#FFFFFF" stroke="#111" stroke-width="6" />
          <g :clip-path="`url(#ballI-${uid})`" fill="#111">
            <polygon points="0,-30 28.5,-9.3 17.6,24.3 -17.6,24.3 -28.5,-9.3" />
            <polygon
              points="0,-28 26.6,-8.7 16.5,22.7 -16.5,22.7 -26.6,-8.7"
              transform="translate(48 -68) rotate(36)"
            />
            <polygon
              points="0,-28 26.6,-8.7 16.5,22.7 -16.5,22.7 -26.6,-8.7"
              transform="translate(80 26) rotate(36)"
            />
            <polygon
              points="0,-28 26.6,-8.7 16.5,22.7 -16.5,22.7 -26.6,-8.7"
              transform="translate(0 84) rotate(36)"
            />
            <polygon
              points="0,-28 26.6,-8.7 16.5,22.7 -16.5,22.7 -26.6,-8.7"
              transform="translate(-80 26) rotate(36)"
            />
            <polygon
              points="0,-28 26.6,-8.7 16.5,22.7 -16.5,22.7 -26.6,-8.7"
              transform="translate(-48 -68) rotate(36)"
            />
          </g>
          <g fill="none" stroke="#111" stroke-width="4">
            <path
              d="M0 -30 L0 -52 M28.5 -9.3 L50 -16 M17.6 24.3 L30 42 M-17.6 24.3 L-30 42 M-28.5 -9.3 L-50 -16"
            />
          </g>
        </g>
        <path
          d="M-90 0 A90 90 0 0 0 0 90 A110 110 0 0 1 -90 0 Z"
          fill="#A5B4FF"
          fill-opacity="0.7"
        />
      </g>
    </g>
    <g fill="none" stroke="#FFFFFF" stroke-width="5" stroke-linecap="round" class="art-tw">
      <path d="M232 196 L252 184 M226 176 L240 156 M246 214 L270 210" />
    </g>
    <g transform="translate(-20 350)">
      <g class="art-kick">
        <path
          d="M0 0 L150 -120"
          stroke="#1B1F6B"
          stroke-width="50"
          stroke-linecap="round"
          fill="none"
        />
        <path d="M92 -73 L128 -102" stroke="#A5B4FF" stroke-width="54" fill="none" />
        <path d="M104 -82 L116 -92" stroke="#FFFFFF" stroke-width="56" fill="none" />
        <path
          d="M126 -150 L186 -186 Q212 -196 216 -172 L212 -160 Q178 -128 146 -112 Z"
          fill="#111"
        />
        <path
          d="M150 -120 L206 -158"
          stroke="#E9B5F5"
          stroke-width="5"
          stroke-linecap="round"
          fill="none"
        />
      </g>
    </g>
  </svg>
</template>
