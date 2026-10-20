<script setup lang="ts">
export type OnboardingArtName =
  'register' | 'photo' | 'about' | 'interests' | 'place' | 'level' | 'result'

defineProps<{ name: OnboardingArtName }>()
</script>

<template>
  <template v-if="name === 'register'">
    <svg viewBox="0 0 460 330" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-register-htI" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="4.5" cy="4.5" r="1.7" fill="#111" fill-opacity="0.4" />
        </pattern>
        <clipPath id="onb-register-ballI"><circle cx="0" cy="0" r="90" /></clipPath>
      </defs>
      <path d="M0 0 H460 V140 Q 230 100 0 160 Z" fill="url(#onb-register-htI)" />
      <path d="M0 150 Q 230 95 460 135" fill="none" stroke="#111" stroke-width="6" />
      <path
        class="flow"
        d="M0 175 Q 230 125 460 160"
        fill="none"
        stroke="#FFF7EC"
        stroke-width="3"
        stroke-dasharray="30 18"
      />
      <path d="M0 250 Q 230 195 460 240 V330 H0 Z" fill="#111" />
      <path
        class="flow"
        d="M0 285 Q 230 232 460 275"
        fill="none"
        stroke="#A5B4FF"
        stroke-width="3"
        stroke-dasharray="40 22"
      />
      <g transform="rotate(-28 230 165)">
        <path class="st" d="M120 150 L260 145 Q266 150 260 155 Z" fill="#FFF7EC" />
        <path class="st2" d="M100 178 L240 173 Q246 178 240 183 Z" fill="#FFFFFF" />
        <path class="st3" d="M140 205 L250 201 Q255 205 250 209 Z" fill="#E9B5F5" />
      </g>
      <g transform="translate(335 110)">
        <g class="zoom">
          <g class="spin">
            <circle cx="0" cy="0" r="90" fill="#FFFFFF" stroke="#111" stroke-width="6" />
            <g clip-path="url(#onb-register-ballI)" fill="#111">
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
      <g fill="none" stroke="#FFFFFF" stroke-width="5" stroke-linecap="round" class="tw">
        <path d="M232 196 L252 184 M226 176 L240 156 M246 214 L270 210" />
      </g>
      <g transform="translate(-20 350)">
        <g class="kick">
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
  <template v-else-if="name === 'photo'">
    <svg viewBox="0 0 460 330" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-photo-htA" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="4.5" cy="4.5" r="1.7" fill="#111" fill-opacity="0.32" />
        </pattern>
        <clipPath id="onb-photo-frameA"><circle cx="230" cy="165" r="104" /></clipPath>
      </defs>
      <rect width="460" height="330" fill="url(#onb-photo-htA)" />
      <g transform="translate(230 165)">
        <polygon
          class="zoom"
          points="0,-150 26,-70 110,-110 70,-30 150,0 70,30 110,110 26,70 0,150 -26,70 -110,110 -70,30 -150,0 -70,-30 -110,-110 -26,-70"
          fill="#F28C28"
        />
      </g>
      <circle cx="230" cy="165" r="104" fill="#FFFFFF" />
      <g clip-path="url(#onb-photo-frameA)">
        <rect x="120" y="60" width="220" height="220" fill="#E9B5F5" />
        <path d="M140 290 Q 150 205 230 198 Q 310 205 320 290 Z" fill="#111" />
        <path
          d="M196 204 L230 240 L264 204"
          fill="none"
          stroke="#F28C28"
          stroke-width="12"
          stroke-linejoin="round"
        />
        <circle cx="230" cy="148" r="40" fill="#111" />
      </g>
      <circle cx="230" cy="165" r="104" fill="none" stroke="#111" stroke-width="7" />
      <g transform="translate(316 248)">
        <circle r="32" fill="#111" />
        <rect
          x="-16"
          y="-10"
          width="32"
          height="22"
          rx="5"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="3"
        />
        <circle cx="0" cy="1" r="6" fill="none" stroke="#FFFFFF" stroke-width="3" />
        <path
          d="M-6 -10 L-3 -15 H3 L6 -10"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="3"
          stroke-linejoin="round"
        />
      </g>
      <g class="drift">
        <circle cx="70" cy="80" r="26" fill="#FFFFFF" stroke="#111" stroke-width="4" />
        <circle cx="70" cy="74" r="9" fill="#111" />
        <path d="M54 98 Q 70 84 86 98" fill="#111" />
      </g>
      <g class="drift2">
        <circle cx="395" cy="95" r="22" fill="#F28C28" stroke="#111" stroke-width="4" />
        <circle cx="395" cy="90" r="8" fill="#111" />
        <path d="M381 110 Q 395 98 409 110" fill="#111" />
      </g>
      <g class="drift2">
        <circle cx="85" cy="260" r="20" fill="#C9D3FF" stroke="#111" stroke-width="4" />
        <circle cx="85" cy="255" r="7" fill="#111" />
        <path d="M72 274 Q 85 263 98 274" fill="#111" />
      </g>
      <g class="tw" transform="translate(400 210)">
        <path d="M0 -12 L3 -3 L12 0 L3 3 L0 12 L-3 3 L-12 0 L-3 -3 Z" fill="#FFFFFF" />
      </g>
      <g class="tw2" transform="translate(140 40)">
        <path d="M0 -10 L2.5 -2.5 L10 0 L2.5 2.5 L0 10 L-2.5 2.5 L-10 0 L-2.5 -2.5 Z" fill="#111" />
      </g>
    </svg>
  </template>
  <template v-else-if="name === 'about'">
    <svg viewBox="0 0 580 420" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-about-htW" width="10" height="10" patternUnits="userSpaceOnUse">
          <circle cx="5" cy="5" r="2" fill="#FFFFFF" fill-opacity="0.45" />
        </pattern>
        <pattern id="onb-about-htD" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="4.5" cy="4.5" r="1.6" fill="#111" fill-opacity="0.35" />
        </pattern>
      </defs>
      <path d="M0 0 H580 V210 Q 290 150 0 230 Z" fill="url(#onb-about-htD)" />
      <circle cx="395" cy="185" r="160" fill="#F28C28" />
      <path
        d="M395 25 A160 160 0 0 1 555 185 A160 160 0 0 1 395 345 Z"
        fill="url(#onb-about-htW)"
      />
      <path d="M-20 300 Q 290 230 600 280 L600 420 L-20 420 Z" fill="#111" />
      <path d="M-20 300 Q 290 230 600 280" fill="none" stroke="#FFFFFF" stroke-width="4" />
      <path
        class="flow"
        d="M-20 345 Q 290 275 600 325"
        fill="none"
        stroke="#A5B4FF"
        stroke-width="4"
        stroke-dasharray="40 24"
      />
      <path
        class="flow"
        d="M-20 390 Q 290 320 600 370"
        fill="none"
        stroke="#E9B5F5"
        stroke-width="4"
        stroke-dasharray="40 24"
      />
      <path class="st" d="M40 150 L190 144 Q196 150 190 156 Z" fill="#FFF7EC" />
      <path class="st2" d="M20 205 L170 200 Q175 205 170 210 Z" fill="#FFF7EC" />
      <path class="st3" d="M70 255 L200 251 Q205 255 200 259 Z" fill="#FFFFFF" />
      <path class="st2" d="M110 110 L220 106 Q224 110 220 114 Z" fill="#E9B5F5" />
      <g transform="translate(232 112) scale(0.82)">
        <g class="run2">
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
        <g class="run">
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
      <g class="tw" transform="translate(520 60)">
        <path
          d="M0 -14 L3.5 -3.5 L14 0 L3.5 3.5 L0 14 L-3.5 3.5 L-14 0 L-3.5 -3.5 Z"
          fill="#FFFFFF"
        />
      </g>
      <g class="tw2" transform="translate(250 50)">
        <path d="M0 -10 L2.5 -2.5 L10 0 L2.5 2.5 L0 10 L-2.5 2.5 L-10 0 L-2.5 -2.5 Z" fill="#111" />
      </g>
    </svg>
  </template>
  <template v-else-if="name === 'interests'">
    <svg viewBox="0 0 460 330" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-interests-htL" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="4.5" cy="4.5" r="1.7" fill="#111" fill-opacity="0.35" />
        </pattern>
      </defs>
      <circle cx="330" cy="120" r="120" fill="#A5B4FF" />
      <circle cx="330" cy="120" r="120" fill="url(#onb-interests-htL)" />
      <path d="M0 255 L460 230 L460 330 L0 330 Z" fill="#111" />
      <path d="M0 255 L460 230" stroke="#FFFFFF" stroke-width="4" />
      <path
        class="flow"
        d="M0 290 L460 268"
        stroke="#E9B5F5"
        stroke-width="3"
        stroke-dasharray="36 20"
      />
      <path class="st" d="M250 120 L350 117 Q354 120 350 123 Z" fill="#FFF7EC" />
      <path class="st2" d="M240 160 L330 157 Q334 160 330 163 Z" fill="#FFFFFF" />
      <path class="st3" d="M110 150 L170 148 Q173 150 170 152 Z" fill="#FFF7EC" />
      <g transform="translate(62 96) scale(0.62)">
        <g class="drift">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 54 L30 92 L34 120" stroke="#111" stroke-width="18" />
            <path d="M12 130 L30 182 L34 232" stroke="#111" stroke-width="24" />
            <path d="M14 46 L12 118" stroke="#FFFFFF" stroke-width="46" />
            <path d="M12 114 L12 136" stroke="#A5B4FF" stroke-width="50" />
            <path d="M10 130 L-8 180 L-14 232" stroke="#111" stroke-width="24" />
            <path d="M12 54 L-2 92 L4 120" stroke="#111" stroke-width="18" />
          </g>
          <ellipse cx="38" cy="236" rx="18" ry="9" fill="#FFFFFF" />
          <ellipse cx="-10" cy="236" rx="18" ry="9" fill="#FFFFFF" />
          <circle cx="16" cy="6" r="23" fill="#111" />
        </g>
      </g>
      <g transform="translate(200 84) scale(0.7)">
        <g class="run2">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 54 L-16 84 L-30 70" stroke="#111" stroke-width="18" />
            <path d="M6 130 L-30 170 L-56 196" stroke="#111" stroke-width="24" />
            <path d="M14 46 L8 118" stroke="#A5B4FF" stroke-width="46" />
            <path d="M8 114 L6 136" stroke="#F28C28" stroke-width="50" />
            <path d="M8 130 L38 168 L34 230" stroke="#111" stroke-width="24" />
            <path d="M12 54 L36 82 L54 64" stroke="#111" stroke-width="18" />
          </g>
          <ellipse cx="-60" cy="198" rx="18" ry="9" fill="#FFFFFF" />
          <ellipse cx="42" cy="234" rx="18" ry="9" fill="#FFFFFF" />
          <circle cx="18" cy="6" r="23" fill="#111" />
        </g>
      </g>
      <g transform="translate(360 54) scale(0.8)">
        <g class="run">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 52 L-30 84 L-58 78" stroke="#111" stroke-width="18" />
            <path d="M0 128 L-44 176 L-96 168" stroke="#111" stroke-width="24" />
            <path d="M10 46 L2 118" stroke="#F28C28" stroke-width="46" />
            <path d="M2 114 L0 136" stroke="#111" stroke-width="50" />
            <path d="M4 132 L52 168 L36 236" stroke="#111" stroke-width="24" />
            <path d="M10 54 L46 86 L76 58" stroke="#111" stroke-width="18" />
          </g>
          <ellipse cx="-102" cy="170" rx="18" ry="9" fill="#E9B5F5" />
          <ellipse cx="46" cy="240" rx="20" ry="9" fill="#E9B5F5" />
          <circle cx="18" cy="8" r="23" fill="#111" />
        </g>
      </g>
      <g
        font-family="Caveat, cursive"
        font-weight="700"
        font-size="22"
        text-anchor="middle"
        fill="#111"
      >
        <g transform="translate(70 312)">
          <rect x="-52" y="-20" width="104" height="30" rx="15" fill="#FFF7EC" />
          <text y="2">{{ $t('onboarding.art.beginner') }}</text>
        </g>
        <g transform="translate(212 312)">
          <rect x="-54" y="-20" width="108" height="30" rx="15" fill="#FFF7EC" />
          <text y="2">{{ $t('onboarding.art.amateur') }}</text>
        </g>
        <g transform="translate(372 312)">
          <rect x="-45" y="-20" width="90" height="30" rx="15" fill="#FFF7EC" />
          <text y="2">{{ $t('onboarding.art.pro') }}</text>
        </g>
      </g>
    </svg>
  </template>
  <template v-else-if="name === 'place'">
    <svg viewBox="0 0 460 330" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-place-htP" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="4.5" cy="4.5" r="1.6" fill="#111" fill-opacity="0.28" />
        </pattern>
      </defs>
      <rect width="460" height="330" fill="url(#onb-place-htP)" />
      <path
        d="M-10 300 C 80 250, 120 300, 200 270 S 330 300, 470 250"
        fill="none"
        stroke="#A5B4FF"
        stroke-width="22"
        stroke-linecap="round"
      />
      <g transform="rotate(-12 230 150)">
        <ellipse cx="230" cy="150" rx="170" ry="104" fill="#F28C28" />
        <ellipse
          class="flow"
          cx="230"
          cy="150"
          rx="155"
          ry="91"
          fill="none"
          stroke="#FFF7EC"
          stroke-width="2.5"
          stroke-dasharray="22 14"
        />
        <ellipse
          cx="230"
          cy="150"
          rx="140"
          ry="78"
          fill="none"
          stroke="#FFF7EC"
          stroke-width="2.5"
        />
        <rect x="110" y="88" width="240" height="124" rx="26" fill="#111" />
        <g fill="none" stroke="#FFFFFF" stroke-width="2.5" stroke-opacity="0.8">
          <path d="M230 92 V208" />
          <circle cx="230" cy="150" r="24" />
          <rect x="114" y="122" width="34" height="56" />
          <rect x="312" y="122" width="34" height="56" />
        </g>
        <g class="drift">
          <circle cx="180" cy="125" r="8" fill="#A5B4FF" />
          <circle cx="200" cy="175" r="8" fill="#A5B4FF" />
          <circle cx="155" cy="150" r="8" fill="#A5B4FF" />
        </g>
        <g class="drift2">
          <circle cx="270" cy="130" r="8" fill="#E9B5F5" />
          <circle cx="285" cy="178" r="8" fill="#E9B5F5" />
          <circle cx="305" cy="150" r="8" fill="#E9B5F5" />
        </g>
        <g class="drift"><circle cx="238" cy="146" r="6" fill="#FFFFFF" /></g>
        <g class="run">
          <circle cx="230" cy="54" r="7" fill="#111" />
          <circle cx="250" cy="55" r="7" fill="#1B1F6B" />
        </g>
      </g>
      <circle class="pulse" cx="60" cy="275" r="46" fill="none" stroke="#111" stroke-width="2.5" />
      <path
        class="flow"
        d="M60 275 C 90 240, 100 220, 120 205"
        fill="none"
        stroke="#111"
        stroke-width="3"
        stroke-dasharray="8 10"
        stroke-linecap="round"
      />
      <g transform="translate(60 275)">
        <circle r="22" fill="#111" />
        <path
          d="M-9 2 L0 -7 L9 2 V9 H-9 Z"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="2.4"
          stroke-linejoin="round"
        />
      </g>
      <g transform="translate(395 70)">
        <g class="jump">
          <path
            d="M0 0 C -18 -18, -18 -44, 0 -46 C 18 -44, 18 -18, 0 0 Z"
            fill="#E9B5F5"
            stroke="#111"
            stroke-width="4"
            stroke-linejoin="round"
          />
          <circle cx="0" cy="-28" r="6" fill="#111" />
        </g>
      </g>
      <g transform="translate(330 92)">
        <rect x="0" y="-18" width="122" height="34" rx="17" fill="#FFFFFF" />
        <text
          x="61"
          y="5"
          text-anchor="middle"
          font-family="Inter Tight, sans-serif"
          font-weight="700"
          font-size="14"
          fill="#111"
        >
          {{ $t('onboarding.art.football') }}
        </text>
      </g>
      <g transform="translate(38 46)">
        <g class="jump2">
          <path
            d="M0 0 C -18 -18, -18 -44, 0 -46 C 18 -44, 18 -18, 0 0 Z"
            fill="#FFFFFF"
            stroke="#111"
            stroke-width="4"
            stroke-linejoin="round"
          />
          <circle cx="0" cy="-28" r="6" fill="#111" />
        </g>
      </g>
      <g transform="translate(14 64)">
        <rect x="0" y="-18" width="108" height="34" rx="17" fill="#FFFFFF" />
        <text
          x="54"
          y="5"
          text-anchor="middle"
          font-family="Inter Tight, sans-serif"
          font-weight="700"
          font-size="14"
          fill="#111"
        >
          {{ $t('onboarding.art.running') }}
        </text>
      </g>
    </svg>
  </template>
  <template v-else-if="name === 'level'">
    <svg viewBox="0 0 460 330" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-level-htG" width="9" height="9" patternUnits="userSpaceOnUse">
          <circle cx="4.5" cy="4.5" r="1.7" fill="#111" fill-opacity="0.35" />
        </pattern>
      </defs>
      <path d="M0 0 H460 V150 Q 230 110 0 170 Z" fill="url(#onb-level-htG)" />
      <circle cx="230" cy="150" r="125" fill="#E9B5F5" />
      <circle cx="230" cy="150" r="125" fill="url(#onb-level-htG)" />
      <path d="M0 262 Q 230 228 460 262 V330 H0 Z" fill="#111" />
      <path
        class="flow"
        d="M0 298 Q 230 262 460 298"
        fill="none"
        stroke="#A5B4FF"
        stroke-width="3"
        stroke-dasharray="36 20"
      />
      <g transform="translate(128 86) scale(0.7)">
        <g class="jump">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 54 L34 92 L40 120" stroke="#111" stroke-width="18" />
            <path d="M12 130 L30 182 L34 232" stroke="#111" stroke-width="24" />
            <path d="M14 46 L12 118" stroke="#FFFFFF" stroke-width="46" />
            <path d="M12 114 L12 136" stroke="#1B1F6B" stroke-width="50" />
            <path d="M10 130 L-8 180 L-14 232" stroke="#111" stroke-width="24" />
            <path d="M12 54 L-10 20 L-16 -16" stroke="#111" stroke-width="18" />
          </g>
          <ellipse cx="38" cy="236" rx="18" ry="9" fill="#F28C28" />
          <ellipse cx="-10" cy="236" rx="18" ry="9" fill="#F28C28" />
          <circle cx="16" cy="6" r="23" fill="#111" />
        </g>
      </g>
      <g transform="translate(222 62) scale(0.8)">
        <g class="jump2">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 130 L30 182 L34 232" stroke="#111" stroke-width="24" />
            <path d="M14 46 L12 118" stroke="#A5B4FF" stroke-width="46" />
            <path d="M12 114 L12 136" stroke="#F28C28" stroke-width="50" />
            <path d="M10 130 L-8 180 L-14 232" stroke="#111" stroke-width="24" />
            <path d="M12 54 L-14 24 L-22 -12" stroke="#111" stroke-width="18" />
            <path d="M16 54 L40 22 L46 -14" stroke="#111" stroke-width="18" />
          </g>
          <ellipse cx="38" cy="236" rx="18" ry="9" fill="#FFFFFF" />
          <ellipse cx="-10" cy="236" rx="18" ry="9" fill="#FFFFFF" />
          <circle cx="16" cy="6" r="23" fill="#111" />
          <path d="M-4 0 Q-34 -4 -46 18 Q-28 10 -10 14 Z" fill="#111" />
        </g>
      </g>
      <g transform="translate(320 96) scale(0.66)">
        <g class="jump">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 54 L-8 92 L-12 120" stroke="#111" stroke-width="18" />
            <path d="M12 130 L30 182 L34 232" stroke="#111" stroke-width="24" />
            <path d="M14 46 L12 118" stroke="#F28C28" stroke-width="46" />
            <path d="M12 114 L12 136" stroke="#FFFFFF" stroke-width="50" />
            <path d="M10 130 L-8 180 L-14 232" stroke="#111" stroke-width="24" />
            <path d="M16 54 L40 22 L48 -14" stroke="#111" stroke-width="18" />
          </g>
          <ellipse cx="38" cy="236" rx="18" ry="9" fill="#E9B5F5" />
          <ellipse cx="-10" cy="236" rx="18" ry="9" fill="#E9B5F5" />
          <circle cx="16" cy="6" r="23" fill="#111" />
        </g>
      </g>
      <g class="tw" transform="translate(70 70)">
        <path
          d="M0 -14 L3.5 -3.5 L14 0 L3.5 3.5 L0 14 L-3.5 3.5 L-14 0 L-3.5 -3.5 Z"
          fill="#FFFFFF"
        />
      </g>
      <g class="tw2" transform="translate(400 60)">
        <path d="M0 -10 L2.5 -2.5 L10 0 L2.5 2.5 L0 10 L-2.5 2.5 L-10 0 L-2.5 -2.5 Z" fill="#111" />
      </g>
    </svg>
  </template>
  <template v-else-if="name === 'result'">
    <svg viewBox="0 0 340 190" class="block h-auto w-full" aria-hidden="true">
      <defs>
        <pattern id="onb-result-htR" width="8" height="8" patternUnits="userSpaceOnUse">
          <circle cx="4" cy="4" r="1.5" fill="#FFFFFF" fill-opacity="0.5" />
        </pattern>
      </defs>
      <g transform="translate(170 52)">
        <polygon
          class="zoom"
          points="0,-62 14,-30 48,-44 32,-12 66,0 32,12 48,44 14,30 0,62 -14,30 -48,44 -32,12 -66,0 -32,-12 -48,-44 -14,-30"
          fill="#F28C28"
        />
        <polygon
          points="0,-62 14,-30 48,-44 32,-12 66,0 32,12 48,44 14,30 0,62 -14,30 -48,44 -32,12 -66,0 -32,-12 -48,-44 -14,-30"
          fill="url(#onb-result-htR)"
        />
      </g>
      <g class="tw" fill="none" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round">
        <path d="M170 -4 V8 M214 14 L206 22 M126 14 L134 22" />
      </g>
      <g class="tw2">
        <rect
          x="40"
          y="30"
          width="12"
          height="6"
          rx="2"
          fill="#E9B5F5"
          transform="rotate(-20 46 33)"
        />
        <rect
          x="292"
          y="40"
          width="12"
          height="6"
          rx="2"
          fill="#FFFFFF"
          transform="rotate(25 298 43)"
        />
        <circle cx="70" cy="80" r="4" fill="#111" />
        <circle cx="280" cy="96" r="4" fill="#F28C28" />
      </g>
      <g transform="translate(110 58) scale(0.52)">
        <g class="jump">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 54 L-24 70 L-44 50" stroke="#111" stroke-width="18" />
            <path d="M8 130 L-20 168 L-4 206" stroke="#111" stroke-width="24" />
            <path d="M14 46 L10 118" stroke="#FFFFFF" stroke-width="46" />
            <path d="M10 114 L10 136" stroke="#1B1F6B" stroke-width="50" />
            <path d="M10 130 L40 160 L30 206" stroke="#111" stroke-width="24" />
            <path d="M14 52 L56 14 L104 -8" stroke="#111" stroke-width="18" />
          </g>
          <circle cx="16" cy="6" r="23" fill="#111" />
        </g>
      </g>
      <g transform="translate(230 58) scale(-0.52 0.52)">
        <g class="jump2">
          <g fill="none" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10 54 L-24 70 L-44 50" stroke="#111" stroke-width="18" />
            <path d="M8 130 L-20 168 L-4 206" stroke="#111" stroke-width="24" />
            <path d="M14 46 L10 118" stroke="#F28C28" stroke-width="46" />
            <path d="M10 114 L10 136" stroke="#111" stroke-width="50" />
            <path d="M10 130 L40 160 L30 206" stroke="#111" stroke-width="24" />
            <path d="M14 52 L56 14 L104 -8" stroke="#111" stroke-width="18" />
          </g>
          <circle cx="16" cy="6" r="23" fill="#111" />
          <path d="M-4 0 Q-34 -4 -46 18 Q-28 10 -10 14 Z" fill="#111" />
        </g>
      </g>
      <path d="M40 184 H300" stroke="#111" stroke-width="5" stroke-linecap="round" />
    </svg>
  </template>
</template>

<style scoped>
/* Анимации иллюстраций — те же, что в макете онбординга. */
@keyframes streak {
  0% {
    transform: translateX(40px);
    opacity: 0;
  }
  25% {
    opacity: 1;
  }
  100% {
    transform: translateX(-80px);
    opacity: 0;
  }
}
@keyframes run {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-9px);
  }
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@keyframes zoom {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
}
@keyframes flow {
  to {
    stroke-dashoffset: -100;
  }
}
@keyframes kick {
  0%,
  100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(4deg);
  }
}
@keyframes drift {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(9px, -6px);
  }
}
@keyframes tw {
  0%,
  100% {
    opacity: 0.25;
    transform: scale(0.7);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}
@keyframes pulse {
  0% {
    transform: scale(0.5);
    opacity: 0.8;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
@keyframes jump {
  0%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-12px);
  }
}
.st {
  animation: streak 1.8s ease-in infinite;
}
.st2 {
  animation: streak 1.8s ease-in 0.6s infinite;
}
.st3 {
  animation: streak 1.8s ease-in 1.2s infinite;
}
.run {
  animation: run 0.9s ease-in-out infinite;
  transform-box: fill-box;
}
.run2 {
  animation: run 0.9s ease-in-out 0.45s infinite;
  transform-box: fill-box;
}
.spin {
  animation: spin 7s linear infinite;
  transform-box: fill-box;
  transform-origin: center;
}
.zoom {
  animation: zoom 2.4s ease-in-out infinite;
  transform-box: fill-box;
  transform-origin: center;
}
.flow {
  animation: flow 3s linear infinite;
}
.kick {
  animation: kick 1.6s ease-in-out infinite;
  transform-box: fill-box;
  transform-origin: 0% 100%;
}
.drift {
  animation: drift 3s ease-in-out infinite;
  transform-box: fill-box;
}
.drift2 {
  animation: drift 3s ease-in-out 1.5s infinite;
  transform-box: fill-box;
}
.tw {
  animation: tw 1.8s ease-in-out infinite;
  transform-box: fill-box;
  transform-origin: center;
}
.tw2 {
  animation: tw 1.8s ease-in-out 0.9s infinite;
  transform-box: fill-box;
  transform-origin: center;
}
.pulse {
  animation: pulse 2.6s ease-out infinite;
  transform-box: fill-box;
  transform-origin: center;
}
.jump {
  animation: jump 1.4s ease-in-out infinite;
  transform-box: fill-box;
}
.jump2 {
  animation: jump 1.4s ease-in-out 0.35s infinite;
  transform-box: fill-box;
}
@media (prefers-reduced-motion: reduce) {
  .st,
  .st2,
  .st3,
  .run,
  .run2,
  .spin,
  .zoom,
  .flow,
  .kick,
  .drift,
  .drift2,
  .tw,
  .tw2,
  .pulse,
  .jump,
  .jump2 {
    animation: none;
  }
}
</style>
