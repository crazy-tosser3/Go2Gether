<script setup lang="ts">
import { useId } from 'vue'

const uid = useId()
</script>

<!-- Командное фото из макета Go2Gether: три ряда спортсменов в форме разных видов спорта. -->
<template>
  <svg
    viewBox="0 0 520 600"
    preserveAspectRatio="xMidYMax slice"
    class="block size-full"
    aria-hidden="true"
  >
    <defs>
      <pattern :id="`htT-${uid}`" width="9" height="9" patternUnits="userSpaceOnUse">
        <circle cx="4.5" cy="4.5" r="1.7" fill="#111" fill-opacity="0.26" />
      </pattern>
    </defs>
    <rect x="-200" y="0" width="920" height="600" :fill="`url(#htT-${uid})`" />
    <circle cx="260" cy="250" r="200" fill="#A5B4FF" />
    <circle cx="260" cy="250" r="200" :fill="`url(#htT-${uid})`" />
    <path d="M-200 62 Q 260 132 720 62" fill="none" stroke="#111" stroke-width="2.5" />
    <g class="art-sway" style="animation-delay: 0s; transform-origin: -166px 68px">
      <path
        d="M-180 68.0 L -152 70.0 L -166 98.0 Z"
        fill="#F28C28"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.2s; transform-origin: -122px 80.1px">
      <path
        d="M-136 80.1 L -108 82.1 L -122 110.1 Z"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.4s; transform-origin: -78px 91px">
      <path
        d="M-92 91.0 L -64 93.0 L -78 121.0 Z"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g
      class="art-sway"
      style="animation-delay: 0.6000000000000001s; transform-origin: -34px 100.6px"
    >
      <path d="M-48 100.6 L -20 102.6 L -34 130.6 Z" fill="#111" stroke="#111" stroke-width="1.5" />
    </g>
    <g class="art-sway" style="animation-delay: 0.8s; transform-origin: 10px 108.9px">
      <path d="M-4 108.9 L 24 110.9 L 10 138.9 Z" fill="#FFF7EC" stroke="#111" stroke-width="1.5" />
    </g>
    <g class="art-sway" style="animation-delay: 0s; transform-origin: 54px 116px">
      <path d="M40 116.0 L 68 118.0 L 54 146.0 Z" fill="#F28C28" stroke="#111" stroke-width="1.5" />
    </g>
    <g class="art-sway" style="animation-delay: 0.2s; transform-origin: 98px 121.8px">
      <path
        d="M84 121.8 L 112 123.8 L 98 151.8 Z"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.4s; transform-origin: 142px 126.2px">
      <path
        d="M128 126.2 L 156 128.2 L 142 156.2 Z"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g
      class="art-sway"
      style="animation-delay: 0.6000000000000001s; transform-origin: 186px 129.4px"
    >
      <path d="M172 129.4 L 200 131.4 L 186 159.4 Z" fill="#111" stroke="#111" stroke-width="1.5" />
    </g>
    <g class="art-sway" style="animation-delay: 0.8s; transform-origin: 230px 131.4px">
      <path
        d="M216 131.4 L 244 133.4 L 230 161.4 Z"
        fill="#FFF7EC"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0s; transform-origin: 274px 132px">
      <path
        d="M260 132.0 L 288 134.0 L 274 162.0 Z"
        fill="#F28C28"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.2s; transform-origin: 318px 131.4px">
      <path
        d="M304 131.4 L 332 133.4 L 318 161.4 Z"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.4s; transform-origin: 362px 129.4px">
      <path
        d="M348 129.4 L 376 131.4 L 362 159.4 Z"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g
      class="art-sway"
      style="animation-delay: 0.6000000000000001s; transform-origin: 406px 126.2px"
    >
      <path d="M392 126.2 L 420 128.2 L 406 156.2 Z" fill="#111" stroke="#111" stroke-width="1.5" />
    </g>
    <g class="art-sway" style="animation-delay: 0.8s; transform-origin: 450px 121.8px">
      <path
        d="M436 121.8 L 464 123.8 L 450 151.8 Z"
        fill="#FFF7EC"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0s; transform-origin: 494px 116px">
      <path
        d="M480 116.0 L 508 118.0 L 494 146.0 Z"
        fill="#F28C28"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.2s; transform-origin: 538px 108.9px">
      <path
        d="M524 108.9 L 552 110.9 L 538 138.9 Z"
        fill="#FFFFFF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.4s; transform-origin: 582px 100.6px">
      <path
        d="M568 100.6 L 596 102.6 L 582 130.6 Z"
        fill="#A5B4FF"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0.6000000000000001s; transform-origin: 626px 91px">
      <path d="M612 91.0 L 640 93.0 L 626 121.0 Z" fill="#111" stroke="#111" stroke-width="1.5" />
    </g>
    <g class="art-sway" style="animation-delay: 0.8s; transform-origin: 670px 80.1px">
      <path
        d="M656 80.1 L 684 82.1 L 670 110.1 Z"
        fill="#FFF7EC"
        stroke="#111"
        stroke-width="1.5"
      />
    </g>
    <g class="art-sway" style="animation-delay: 0s; transform-origin: 714px 68px">
      <path d="M700 68.0 L 728 70.0 L 714 98.0 Z" fill="#F28C28" stroke="#111" stroke-width="1.5" />
    </g>
    <rect x="-200" y="286" width="920" height="40" fill="#FFF7EC" fill-opacity="0.9" />
    <rect x="-200" y="286" width="920" height="5" fill="#111" />
    <rect x="-200" y="404" width="920" height="40" fill="#FFF7EC" fill-opacity="0.9" />
    <rect x="-200" y="404" width="920" height="5" fill="#111" />
    <rect x="-200" y="520" width="920" height="80" fill="#1B1F6B" />
    <path
      d="M-200 540 H 720"
      stroke="#FFFFFF"
      stroke-opacity="0.5"
      stroke-width="2"
      stroke-dasharray="26 16"
      class="art-flow"
    />
    <g transform="translate(62 300) scale(0.74)">
      <g class="art-breathe" style="animation-delay: 0s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-13.2 -40 L -13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M13.2 -40 L 13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#F28C28"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#F28C28"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-24 -116 H 24 L 25 -66 H 3 L 0 -84 L -3 -66 H -25 Z" fill="#111" />
        <rect x="-6" y="-184" width="12" height="16" fill="#F2C4A0" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#F2C4A0"
        />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -165 8 -174 Q 15 -174 27 -168 L 24 -112 H -24 Z"
          fill="#111"
        />
        <rect x="-25.5" y="-148" width="51" height="8" fill="#A5B4FF" />
        <path d="M-27 -168 Q -36 -160 -37 -142 L -26 -140 Z" fill="#111" />
        <path d="M27 -168 Q 36 -160 37 -142 L 26 -140 Z" fill="#111" />
        <path
          d="M-27 -164 L -31 -134"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -134 L 14 -138"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -164 L -29.2 -147.5"
          fill="none"
          stroke="#111"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L -14 -136"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 29.2 -146.4"
          fill="none"
          stroke="#111"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="16"
          cy="-138"
          r="6"
          fill="#F2C4A0"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="-16"
          cy="-136"
          r="6"
          fill="#F2C4A0"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#F2C4A0" />
          <circle cx="15" cy="1" r="4" fill="#F2C4A0" />
          <circle cx="0" cy="0" r="15" fill="#F2C4A0" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#2A1A12"
          />
          <path d="M-19 -1 Q -19 -25 2 -24 Q 21 -21 19 -1 Z" fill="#F28C28" />
          <path
            d="M-6 -22 L -3 -6 M5 -23 L 6 -6"
            fill="none"
            stroke="#111"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M-19 -1 H 19"
            fill="none"
            stroke="#111"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M-12 -1 L -6 12 M12 -1 L 6 12"
            fill="none"
            stroke="#111"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(141 300) scale(0.74)">
      <g class="art-breathe" style="animation-delay: 0.35s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#A5C8FF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#A5C8FF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -32 -102"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 32 -102"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#8D5A3C" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#8D5A3C"
        />
        <path
          d="M-14 -166 L -10 -166 Q 0 -152 10 -166 L 14 -166 L 22 -148 L 22 -112 L 24 -98 L 6 -92 L 0 -104 L -6 -92 L -24 -98 L -22 -112 L -22 -148 Z"
          fill="#1B1F6B"
        />
        <path
          d="M-27 -168 Q -18 -176 -6 -172 L -14 -116 L -26 -118 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="-32"
          cy="-97"
          r="6"
          fill="#8D5A3C"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="32"
          cy="-97"
          r="6"
          fill="#8D5A3C"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#8D5A3C" />
          <circle cx="15" cy="1" r="4" fill="#8D5A3C" />
          <circle cx="0" cy="0" r="15" fill="#8D5A3C" />
          <path d="M-16 2 Q -17 -21 0 -20 Q 17 -21 16 2 Z" fill="#F28C28" />
          <rect x="-14" y="-6" width="28" height="7" rx="3.5" fill="#111" />
          <circle cx="-6.5" cy="-2.5" r="4.2" fill="#A5C8FF" stroke="#111" stroke-width="1.5" />
          <circle cx="6.5" cy="-2.5" r="4.2" fill="#A5C8FF" stroke="#111" stroke-width="1.5" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(220 300) scale(0.74)">
      <g class="art-breathe" style="animation-delay: 0.7s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#B97A56"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#B97A56"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-13.2 -40 L -13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#B97A56"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#B97A56"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M13.2 -40 L 13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-26 -116 H 26 L 30 -60 H 4 L 0 -82 L -4 -60 H -30 Z" fill="#A5B4FF" />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#B97A56"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -32 -102"
          fill="none"
          stroke="#B97A56"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#B97A56"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 32 -102"
          fill="none"
          stroke="#B97A56"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#B97A56" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#B97A56"
        />
        <path
          d="M-19 -172 Q -12 -171 -8 -173 Q 0 -160 8 -173 Q 12 -171 19 -172 L 25 -150 L 24 -112 H -24 L -25 -150 Z"
          fill="#A5B4FF"
        />
        <text
          x="0"
          y="-128"
          text-anchor="middle"
          font-family="Exo 2, sans-serif"
          font-style="italic"
          font-weight="900"
          font-size="21"
          fill="#111"
        >
          23
        </text>
        <circle
          cx="-32"
          cy="-97"
          r="6"
          fill="#B97A56"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="32"
          cy="-97"
          r="6"
          fill="#B97A56"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <rect x="-36" y="-108" width="9" height="7" rx="2" fill="#F28C28" />
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#B97A56" />
          <circle cx="15" cy="1" r="4" fill="#B97A56" />
          <circle cx="0" cy="0" r="15" fill="#B97A56" />
          <path d="M-15 -2 Q -15 -16 0 -16 Q 15 -16 15 -2 Q 0 -10 -15 -2 Z" fill="#111" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(299 300) scale(0.74)">
      <g class="art-breathe" style="animation-delay: 1.0499999999999998s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-13.2 -40 L -13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-20.6" y="-60" width="16" height="14" rx="5" fill="#1B1F6B" />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M13.2 -40 L 13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="4.6" y="-60" width="16" height="14" rx="5" fill="#1B1F6B" />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-25 -116 H 25 L 28 -74 H 3 L 0 -88 L -3 -74 H -28 Z" fill="#E9B5F5" />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -32 -102"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -164 L -29.2 -146.4"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 32 -102"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 29.2 -146.4"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#F7D9C4" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#F7D9C4"
        />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -165 8 -174 Q 15 -174 27 -168 L 24 -112 H -24 Z"
          fill="#FFFFFF"
        />
        <text
          x="0"
          y="-128"
          text-anchor="middle"
          font-family="Exo 2, sans-serif"
          font-style="italic"
          font-weight="900"
          font-size="21"
          fill="#E2432B"
        >
          7
        </text>
        <path d="M-27 -168 Q -36 -160 -37 -142 L -26 -140 Z" fill="#FFFFFF" />
        <path d="M27 -168 Q 36 -160 37 -142 L 26 -140 Z" fill="#FFFFFF" />
        <circle
          cx="-32"
          cy="-97"
          r="6"
          fill="#F7D9C4"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="32"
          cy="-97"
          r="6"
          fill="#F7D9C4"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -197)">
          <path d="M11 -9 Q 27 -4 23 26 Q 19 9 9 1 Z" fill="#C7843E" />
          <circle cx="-15" cy="1" r="4" fill="#F7D9C4" />
          <circle cx="15" cy="1" r="4" fill="#F7D9C4" />
          <circle cx="0" cy="0" r="15" fill="#F7D9C4" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#C7843E"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(378 300) scale(0.74)">
      <g class="art-breathe" style="animation-delay: 1.4s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#111"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#111"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#111"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#111"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#E9B5F5"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#E9B5F5"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#D9A07A"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -32 -102"
          fill="none"
          stroke="#D9A07A"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#D9A07A"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 32 -102"
          fill="none"
          stroke="#D9A07A"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#D9A07A" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#D9A07A"
        />
        <path d="M-24 -118 H 24 L 25 -100 H -25 Z" fill="#111" />
        <path
          d="M-18 -171 Q 0 -158 18 -171 L 24 -156 L 22 -132 H -22 L -24 -156 Z"
          fill="#E9B5F5"
        />
        <circle
          cx="-32"
          cy="-97"
          r="6"
          fill="#D9A07A"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="32"
          cy="-97"
          r="6"
          fill="#D9A07A"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(38 -118)">
          <rect
            x="-6"
            y="-26"
            width="16"
            height="56"
            rx="8"
            fill="#C9D3FF"
            stroke="#111"
            stroke-width="1.5"
          />
          <ellipse cx="2" cy="-26" rx="8" ry="3" fill="#A5B4FF" stroke="#111" stroke-width="1.2" />
        </g>
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#D9A07A" />
          <circle cx="15" cy="1" r="4" fill="#D9A07A" />
          <circle cx="0" cy="0" r="15" fill="#D9A07A" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#111"
          />
          <circle cx="0" cy="-19" r="7" fill="#111" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(457 300) scale(0.74)">
      <g class="art-breathe" style="animation-delay: 1.75s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#E8B48F"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#E8B48F"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#E8B48F"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#E8B48F"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#111"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#111"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-26 -118 H 26 L 29 -76 H 3 L 0 -90 L -3 -76 H -29 Z" fill="#E2432B" />
        <rect x="-26" y="-118" width="52" height="9" fill="#FFFFFF" />
        <rect x="-6" y="-184" width="12" height="16" fill="#E8B48F" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#E8B48F"
        />
        <path
          d="M-19 -172 Q -12 -171 -8 -173 Q 0 -160 8 -173 Q 12 -171 19 -172 L 25 -150 L 24 -112 H -24 L -25 -150 Z"
          fill="#E2432B"
        />
        <path
          d="M-27 -164 L -32 -134"
          fill="none"
          stroke="#E8B48F"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 -134 L -17 -158"
          fill="none"
          stroke="#E8B48F"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 32 -134"
          fill="none"
          stroke="#E8B48F"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 -134 L 17 -158"
          fill="none"
          stroke="#E8B48F"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="-15" cy="-163" r="12.5" fill="#E2432B" stroke="#111" stroke-width="1.5" />
        <circle cx="15" cy="-163" r="12.5" fill="#E2432B" stroke="#111" stroke-width="1.5" />
        <rect x="-22" y="-153" width="14" height="5" fill="#FFFFFF" />
        <rect x="8" y="-153" width="14" height="5" fill="#FFFFFF" />
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#E8B48F" />
          <circle cx="15" cy="1" r="4" fill="#E8B48F" />
          <circle cx="0" cy="0" r="15" fill="#E8B48F" />
          <path d="M-15 -2 Q -15 -16 0 -16 Q 15 -16 15 -2 Q 0 -10 -15 -2 Z" fill="#3A2414" />
          <path d="M-15.5 -7 H 15.5 V -1.5 H -15.5 Z" fill="#FFFFFF" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(100 420) scale(0.84)">
      <g class="art-breathe" style="animation-delay: 0.2s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-13.2 -40 L -13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M13.2 -40 L 13.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 -116 H 24 L 34 -80 H -34 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -24 -112"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -164 L -29.2 -146.4"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 24 -112"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 29.2 -146.4"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#F7D9C4" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#F7D9C4"
        />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -165 8 -174 Q 15 -174 27 -168 L 24 -112 H -24 Z"
          fill="#FFFFFF"
        />
        <path
          d="M-8 -174 L 0 -160 L 8 -174"
          fill="none"
          stroke="#A5B4FF"
          stroke-width="3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-27 -168 Q -36 -160 -37 -142 L -26 -140 Z" fill="#FFFFFF" />
        <path d="M27 -168 Q 36 -160 37 -142 L 26 -140 Z" fill="#FFFFFF" />
        <g transform="translate(0 -197)">
          <path d="M11 -9 Q 27 -4 23 26 Q 19 9 9 1 Z" fill="#6B3A1E" />
          <circle cx="-15" cy="1" r="4" fill="#F7D9C4" />
          <circle cx="15" cy="1" r="4" fill="#F7D9C4" />
          <circle cx="0" cy="0" r="15" fill="#F7D9C4" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#6B3A1E"
          />
          <path d="M-16 -9 H 16 V -4 H -16 Z" fill="#A5B4FF" />
          <path d="M-6 -6 H 26 Q 26 -1 22 -1 H -6 Z" fill="#A5B4FF" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(180 420) scale(0.84)">
      <g class="art-breathe" style="animation-delay: 0.5s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-25 -116 H 25 L 28 -74 H 3 L 0 -88 L -3 -74 H -28 Z" fill="#111" />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -24 -112"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#8D5A3C" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#8D5A3C"
        />
        <path
          d="M-19 -172 Q -12 -171 -8 -173 Q 0 -160 8 -173 Q 12 -171 19 -172 L 25 -150 L 24 -112 H -24 L -25 -150 Z"
          fill="#F28C28"
        />
        <rect
          x="-13"
          y="-152"
          width="26"
          height="19"
          rx="2"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.2"
        />
        <text
          x="0"
          y="-138"
          text-anchor="middle"
          font-family="Inter Tight, sans-serif"
          font-weight="800"
          font-size="11"
          fill="#111"
        >
          214
        </text>
        <g class="art-wave" style="transform-origin: 27px -164px">
          <path
            d="M27 -164 L 44 -186"
            fill="none"
            stroke="#8D5A3C"
            stroke-width="11"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M44 -186 L 48 -218"
            fill="none"
            stroke="#8D5A3C"
            stroke-width="9.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <circle
            cx="48"
            cy="-223"
            r="6"
            fill="#8D5A3C"
            stroke="#111"
            stroke-opacity="0.25"
            stroke-width="1"
          />
        </g>
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#8D5A3C" />
          <circle cx="15" cy="1" r="4" fill="#8D5A3C" />
          <circle cx="0" cy="0" r="15" fill="#8D5A3C" />
          <circle cx="-12" cy="-8" r="6.5" fill="#111" />
          <circle cx="-6" cy="-15" r="6.5" fill="#111" />
          <circle cx="3" cy="-17" r="6.5" fill="#111" />
          <circle cx="11" cy="-12" r="6.5" fill="#111" />
          <circle cx="15" cy="-3" r="6.5" fill="#111" />
          <circle cx="-15" cy="-1" r="6.5" fill="#111" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(260 420) scale(0.84)">
      <g class="art-breathe" style="animation-delay: 0.8s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#111"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#111"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-25 -116 H 25 L 22 -10 H 3 L 0 -90 L -3 -10 H -22 Z" fill="#2A2F45" />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -24 -112"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 24 -112"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#F2C4A0" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#F2C4A0"
        />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -165 8 -174 Q 15 -174 27 -168 L 24 -112 H -24 Z"
          fill="#1B1F6B"
        />
        <rect x="-24" y="-118" width="48" height="6" fill="#151A55" />
        <g transform="translate(0 -140)" fill="#FFFFFF">
          <circle cx="0" cy="-9" r="4.5" />
          <path d="M-4.5 -5 H4.5 L3.5 4 H-3.5 Z M-7 5 H7 V9 H-7 Z" />
        </g>
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#F2C4A0" />
          <circle cx="15" cy="1" r="4" fill="#F2C4A0" />
          <circle cx="0" cy="0" r="15" fill="#F2C4A0" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#9A6B3D"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(340 420) scale(0.84)">
      <g class="art-breathe" style="animation-delay: 1.0999999999999999s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#B97A56"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#B97A56"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#B97A56"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#B97A56"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#E9B5F5"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#E9B5F5"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-25 -116 H 25 L 22 -10 H 3 L 0 -90 L -3 -10 H -22 Z" fill="#1B1F6B" />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#111"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -24 -112"
          fill="none"
          stroke="#111"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#111"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 24 -112"
          fill="none"
          stroke="#111"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-6" y="-184" width="12" height="16" fill="#B97A56" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#B97A56"
        />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -165 8 -174 Q 15 -174 27 -168 L 24 -112 H -24 Z"
          fill="#111"
        />
        <path
          d="M-14 -172 Q 0 -154 14 -172"
          fill="none"
          stroke="#3A3A3A"
          stroke-width="4"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-15 -132 H 15 L 13 -116 H -13 Z" fill="#2A2A2A" />
        <g transform="translate(0 -197)">
          <circle cx="-15" cy="1" r="4" fill="#B97A56" />
          <circle cx="15" cy="1" r="4" fill="#B97A56" />
          <circle cx="0" cy="0" r="15" fill="#B97A56" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#2A1A12"
          />
          <path
            d="M-17 2 Q -17 -21 0 -21 Q 17 -21 17 2"
            fill="none"
            stroke="#E9B5F5"
            stroke-width="4.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <rect x="-21" y="-6" width="8" height="14" rx="3" fill="#E9B5F5" />
          <rect x="13" y="-6" width="8" height="14" rx="3" fill="#E9B5F5" />
          <path
            d="M-17 6 Q -14 15 -5 15"
            fill="none"
            stroke="#E9B5F5"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(420 420) scale(0.84)">
      <g class="art-breathe" style="animation-delay: 1.4s">
        <ellipse cx="0" cy="0" rx="32" ry="6" fill="#111" fill-opacity="0.18" />
        <path
          d="M-12 -104 L -12.6 -52"
          fill="none"
          stroke="#D9A07A"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-12.6 -52 L -13.4 -10"
          fill="none"
          stroke="#D9A07A"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-13.2 -40 L -13.4 -9"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12 -104 L 12.6 -52"
          fill="none"
          stroke="#D9A07A"
          stroke-width="16"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M12.6 -52 L 13.4 -10"
          fill="none"
          stroke="#D9A07A"
          stroke-width="12.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M13.2 -40 L 13.4 -9"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="13.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 0 Q -25 -9 -17 -10 H -9 Q -2 -9 -2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M24 0 Q 25 -9 17 -10 H 9 Q 2 -9 2 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path d="M-25 -116 H 25 L 28 -74 H 3 L 0 -88 L -3 -74 H -28 Z" fill="#111" />
        <path
          d="M-27 -164 L -31 -132"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-31 -132 L -24 -112"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="-24" cy="-112" r="5.2" fill="#111" />
        <path
          d="M27 -164 L 31 -132"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M31 -132 L 24 -112"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="24" cy="-112" r="5.2" fill="#111" />
        <rect x="-6" y="-184" width="12" height="16" fill="#D9A07A" />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -170 8 -174 Q 15 -174 27 -168 L 24 -112 L 25 -104 H -25 L -24 -112 Z"
          fill="#D9A07A"
        />
        <path
          d="M-27 -168 Q -15 -174 -8 -174 Q 0 -165 8 -174 Q 15 -174 27 -168 L 24 -112 H -24 Z"
          fill="#E9B5F5"
        />
        <text
          x="0"
          y="-128"
          text-anchor="middle"
          font-family="Exo 2, sans-serif"
          font-style="italic"
          font-weight="900"
          font-size="21"
          fill="#111"
        >
          1
        </text>
        <g transform="translate(0 -197)">
          <path d="M-17 30 Q -21 -18 0 -18 Q 21 -18 17 30 Z" fill="#2A1A12" />
          <circle cx="-15" cy="1" r="4" fill="#D9A07A" />
          <circle cx="15" cy="1" r="4" fill="#D9A07A" />
          <circle cx="0" cy="0" r="15" fill="#D9A07A" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#2A1A12"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g class="art-breathe" style="animation-delay: 0.5s">
      <path d="M66 326 H 454 L 458 392 H 62 Z" fill="#111" />
      <text
        x="260"
        y="360"
        text-anchor="middle"
        font-family="Exo 2, sans-serif"
        font-style="italic"
        font-weight="900"
        font-size="29"
        fill="#FFFFFF"
      >
        КОМАНДА GO2GETHER
      </text>
      <text
        x="260"
        y="384"
        text-anchor="middle"
        font-family="Caveat, cursive"
        font-weight="700"
        font-size="21"
        fill="#F28C28"
      >
        у каждого свой спорт
      </text>
      <circle
        cx="79.8"
        cy="327.9"
        r="5.2"
        fill="#F7D9C4"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="120.2"
        cy="327.9"
        r="5.2"
        fill="#F7D9C4"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="159.8"
        cy="327.9"
        r="5.2"
        fill="#8D5A3C"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="239.8"
        cy="327.9"
        r="5.2"
        fill="#F2C4A0"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="280.2"
        cy="327.9"
        r="5.2"
        fill="#F2C4A0"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="319.8"
        cy="327.9"
        r="5.2"
        fill="#B97A56"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="360.2"
        cy="327.9"
        r="5.2"
        fill="#B97A56"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="399.8"
        cy="327.9"
        r="5.2"
        fill="#D9A07A"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
      <circle
        cx="440.2"
        cy="327.9"
        r="5.2"
        fill="#D9A07A"
        stroke="#111"
        stroke-opacity="0.3"
        stroke-width="1"
      />
    </g>
    <g transform="translate(62 586) scale(0.96)">
      <g class="art-breathe" style="animation-delay: 0.1s">
        <ellipse cx="0" cy="0" rx="44" ry="7" fill="#111" fill-opacity="0.2" />
        <path
          d="M-17 -54 L -20.4 -10"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-19.0 -38 L -20.4 -9"
          fill="none"
          stroke="#F28C28"
          stroke-width="14"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M17 -54 L 20.4 -10"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M19.0 -38 L 20.4 -9"
          fill="none"
          stroke="#F28C28"
          stroke-width="14"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 0 Q -33 -9 -25 -10 H -17 Q -10 -9 -10 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 0 Q 33 -9 25 -10 H 17 Q 10 -9 10 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="-17" cy="-56" r="10" fill="#F2C4A0" />
        <circle cx="17" cy="-56" r="10" fill="#F2C4A0" />
        <path
          d="M-27 -82 H 27 L 30 -60 Q 17 -54 4 -60 L 0 -64 L -4 -60 Q -17 -54 -30 -60 Z"
          fill="#111"
        />
        <rect x="-6" y="-154" width="12" height="16" fill="#F2C4A0" />
        <path
          d="M-27 -140 Q -15 -146 -8 -146 Q 0 -137 8 -146 Q 15 -146 27 -140 L 24 -80 H -24 Z"
          fill="#F28C28"
        />
        <text
          x="0"
          y="-100"
          text-anchor="middle"
          font-family="Exo 2, sans-serif"
          font-style="italic"
          font-weight="900"
          font-size="21"
          fill="#111"
        >
          10
        </text>
        <path d="M-27 -140 Q -36 -132 -37 -114 L -26 -112 Z" fill="#F28C28" />
        <path d="M27 -140 Q 36 -132 37 -114 L 26 -112 Z" fill="#F28C28" />
        <path
          d="M-27 -136 L -32 -104"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 -104 L -19 -64"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -136 L -29.75 -118.4"
          fill="none"
          stroke="#F28C28"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 32 -104"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 -104 L 19 -64"
          fill="none"
          stroke="#F2C4A0"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 29.75 -118.4"
          fill="none"
          stroke="#F28C28"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="-18"
          cy="-61"
          r="6"
          fill="#F2C4A0"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="18"
          cy="-61"
          r="6"
          fill="#F2C4A0"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g class="art-jump">
          <circle cx="0" cy="-14" r="13" fill="#FFFFFF" stroke="#111" stroke-width="2.5" />
          <path d="M0 -19 L5 -15.5 L3.5 -9.5 L-3.5 -9.5 L-5 -15.5 Z" fill="#111" />
        </g>
        <g transform="translate(0 -167)">
          <circle cx="-15" cy="1" r="4" fill="#F2C4A0" />
          <circle cx="15" cy="1" r="4" fill="#F2C4A0" />
          <circle cx="0" cy="0" r="15" fill="#F2C4A0" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#C7843E"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(161 586) scale(0.96)">
      <g class="art-breathe" style="animation-delay: 0.5s">
        <ellipse cx="0" cy="0" rx="44" ry="7" fill="#111" fill-opacity="0.2" />
        <path
          d="M-17 -54 L -20.4 -10"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-19.0 -38 L -20.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="14"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M17 -54 L 20.4 -10"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M19.0 -38 L 20.4 -9"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="14"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 0 Q -33 -9 -25 -10 H -17 Q -10 -9 -10 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 0 Q 33 -9 25 -10 H 17 Q 10 -9 10 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="-17" cy="-56" r="10" fill="#8D5A3C" />
        <circle cx="17" cy="-56" r="10" fill="#8D5A3C" />
        <path
          d="M-27 -82 H 27 L 30 -60 Q 17 -54 4 -60 L 0 -64 L -4 -60 Q -17 -54 -30 -60 Z"
          fill="#E2432B"
        />
        <rect x="-6" y="-154" width="12" height="16" fill="#8D5A3C" />
        <path
          d="M-27 -140 Q -15 -146 -8 -146 Q 0 -137 8 -146 Q 15 -146 27 -140 L 24 -80 H -24 Z"
          fill="#FFFFFF"
        />
        <text
          x="0"
          y="-100"
          text-anchor="middle"
          font-family="Exo 2, sans-serif"
          font-style="italic"
          font-weight="900"
          font-size="21"
          fill="#1B1F6B"
        >
          5
        </text>
        <path d="M-27 -140 Q -36 -132 -37 -114 L -26 -112 Z" fill="#FFFFFF" />
        <path d="M27 -140 Q 36 -132 37 -114 L 26 -112 Z" fill="#FFFFFF" />
        <path
          d="M-27 -136 L -32 -104"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 -104 L -19 -64"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -136 L -29.75 -118.4"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 32 -104"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 -104 L 19 -64"
          fill="none"
          stroke="#8D5A3C"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 29.75 -118.4"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="-18"
          cy="-61"
          r="6"
          fill="#8D5A3C"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="18"
          cy="-61"
          r="6"
          fill="#8D5A3C"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -167)">
          <path d="M11 -9 Q 27 -4 23 26 Q 19 9 9 1 Z" fill="#111" />
          <circle cx="-15" cy="1" r="4" fill="#8D5A3C" />
          <circle cx="15" cy="1" r="4" fill="#8D5A3C" />
          <circle cx="0" cy="0" r="15" fill="#8D5A3C" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#111"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(260 586) scale(0.96)">
      <g class="art-breathe" style="animation-delay: 0.9s">
        <ellipse cx="0" cy="0" rx="44" ry="7" fill="#111" fill-opacity="0.2" />
        <path
          d="M-17 -54 L -20.4 -10"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M17 -54 L 20.4 -10"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 0 Q -33 -9 -25 -10 H -17 Q -10 -9 -10 -2 V 0 Z"
          fill="#111"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 0 Q 33 -9 25 -10 H 17 Q 10 -9 10 -2 V 0 Z"
          fill="#111"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="-17" cy="-56" r="10" fill="#1B1F6B" />
        <circle cx="17" cy="-56" r="10" fill="#1B1F6B" />
        <path
          d="M-27 -82 H 27 L 30 -60 Q 17 -54 4 -60 L 0 -64 L -4 -60 Q -17 -54 -30 -60 Z"
          fill="#1B1F6B"
        />
        <rect x="-6" y="-154" width="12" height="16" fill="#E8B48F" />
        <path
          d="M-27 -140 Q -15 -146 -8 -146 Q 0 -137 8 -146 Q 15 -146 27 -140 L 24 -80 H -24 Z"
          fill="#A5B4FF"
        />
        <path
          d="M0 -140 V -80"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-24 -132 V -80 M24 -132 V -80"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-7 -146 Q 0 -120 7 -146"
          fill="none"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect
          x="-4"
          y="-126"
          width="9"
          height="6"
          rx="2"
          fill="#C9D3FF"
          stroke="#111"
          stroke-width="1.2"
        />
        <path
          d="M-27 -136 L -32 -104"
          fill="none"
          stroke="#A5B4FF"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 -104 L -19 -64"
          fill="none"
          stroke="#A5B4FF"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 32 -104"
          fill="none"
          stroke="#A5B4FF"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 -104 L 19 -64"
          fill="none"
          stroke="#A5B4FF"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="-18"
          cy="-61"
          r="6"
          fill="#E8B48F"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="18"
          cy="-61"
          r="6"
          fill="#E8B48F"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -167)">
          <circle cx="-15" cy="1" r="4" fill="#E8B48F" />
          <circle cx="15" cy="1" r="4" fill="#E8B48F" />
          <circle cx="0" cy="0" r="15" fill="#E8B48F" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#2A1A12"
          />
          <path d="M-16 -4 Q 0 -24 16 -4 Z" fill="#1B1F6B" />
          <path d="M-4 -6 H 24 Q 25 -2 22 -1 H -4 Z" fill="#1B1F6B" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(359 586) scale(0.96)">
      <g class="art-breathe" style="animation-delay: 1.3000000000000003s">
        <ellipse cx="0" cy="0" rx="44" ry="7" fill="#111" fill-opacity="0.2" />
        <path
          d="M-17 -54 L -20.4 -10"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M17 -54 L 20.4 -10"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 0 Q -33 -9 -25 -10 H -17 Q -10 -9 -10 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 0 Q 33 -9 25 -10 H 17 Q 10 -9 10 -2 V 0 Z"
          fill="#FFFFFF"
          stroke="#111"
          stroke-width="1.3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="-17" cy="-56" r="10" fill="#F7D9C4" />
        <circle cx="17" cy="-56" r="10" fill="#F7D9C4" />
        <path
          d="M-27 -82 H 27 L 30 -60 Q 17 -54 4 -60 L 0 -64 L -4 -60 Q -17 -54 -30 -60 Z"
          fill="#1B1F6B"
        />
        <rect x="-6" y="-154" width="12" height="16" fill="#F7D9C4" />
        <path
          d="M-27 -140 Q -15 -146 -8 -146 Q 0 -137 8 -146 Q 15 -146 27 -140 L 24 -80 H -24 Z"
          fill="#E9B5F5"
        />
        <text
          x="0"
          y="-100"
          text-anchor="middle"
          font-family="Exo 2, sans-serif"
          font-style="italic"
          font-weight="900"
          font-size="21"
          fill="#111"
        >
          3
        </text>
        <path d="M-27 -140 Q -36 -132 -37 -114 L -26 -112 Z" fill="#E9B5F5" />
        <path d="M27 -140 Q 36 -132 37 -114 L 26 -112 Z" fill="#E9B5F5" />
        <path
          d="M-27 -136 L -33 -104"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-33 -104 L -14 -82"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-27 -136 L -30.3 -118.4"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 33 -104"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M33 -104 L 14 -82"
          fill="none"
          stroke="#F7D9C4"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 30.3 -118.4"
          fill="none"
          stroke="#E9B5F5"
          stroke-width="15"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle cx="0" cy="-80" r="17" fill="#FFFFFF" stroke="#111" stroke-width="2" />
        <path
          d="M0 -80 Q 5 -92 -3 -97 M0 -80 Q -12 -78 -16 -70 M0 -80 Q 12 -76 14 -66"
          fill="none"
          stroke="#1B1F6B"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="-15"
          cy="-82"
          r="6"
          fill="#F7D9C4"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="15"
          cy="-82"
          r="6"
          fill="#F7D9C4"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -167)">
          <path d="M-17 30 Q -21 -18 0 -18 Q 21 -18 17 30 Z" fill="#C7843E" />
          <circle cx="-15" cy="1" r="4" fill="#F7D9C4" />
          <circle cx="15" cy="1" r="4" fill="#F7D9C4" />
          <circle cx="0" cy="0" r="15" fill="#F7D9C4" />
          <path
            d="M-15 -1 Q -16 -18 0 -17 Q 16 -18 15 -1 Q 9 -11 -2 -9 Q -10 -8 -15 -1 Z"
            fill="#C7843E"
          />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g transform="translate(458 586) scale(0.96)">
      <g class="art-breathe" style="animation-delay: 1.7000000000000002s">
        <ellipse cx="0" cy="0" rx="44" ry="7" fill="#111" fill-opacity="0.2" />
        <path
          d="M-17 -54 L -20.4 -10"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M17 -54 L 20.4 -10"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="13"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <ellipse cx="-21" cy="-4" rx="9" ry="5" fill="#B97A56" />
        <ellipse cx="21" cy="-4" rx="9" ry="5" fill="#B97A56" />
        <circle cx="-17" cy="-56" r="10" fill="#FFFFFF" />
        <circle cx="17" cy="-56" r="10" fill="#FFFFFF" />
        <path
          d="M-27 -82 H 27 L 30 -60 Q 17 -54 4 -60 L 0 -64 L -4 -60 Q -17 -54 -30 -60 Z"
          fill="#FFFFFF"
        />
        <rect x="-6" y="-154" width="12" height="16" fill="#B97A56" />
        <path
          d="M-27 -140 Q -15 -146 -8 -146 Q 0 -137 8 -146 Q 15 -146 27 -140 L 24 -80 H -24 Z"
          fill="#FFFFFF"
        />
        <path
          d="M-8 -145 L 8 -110 M8 -145 L -4 -120"
          fill="none"
          stroke="#D8D8D8"
          stroke-width="3"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <rect x="-25" y="-92" width="50" height="8" fill="#111" />
        <path
          d="M-27 -136 L -32 -104"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M-32 -104 L -19 -64"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M27 -136 L 32 -104"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="11"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M32 -104 L 19 -64"
          fill="none"
          stroke="#FFFFFF"
          stroke-width="9.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <circle
          cx="-18"
          cy="-61"
          r="6"
          fill="#B97A56"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <circle
          cx="18"
          cy="-61"
          r="6"
          fill="#B97A56"
          stroke="#111"
          stroke-opacity="0.25"
          stroke-width="1"
        />
        <g transform="translate(0 -167)">
          <circle cx="-15" cy="1" r="4" fill="#B97A56" />
          <circle cx="15" cy="1" r="4" fill="#B97A56" />
          <circle cx="0" cy="0" r="15" fill="#B97A56" />
          <path d="M-15 -2 Q -15 -16 0 -16 Q 15 -16 15 -2 Q 0 -10 -15 -2 Z" fill="#111" />
          <circle cx="-5.5" cy="1" r="1.8" fill="#111" />
          <circle cx="5.5" cy="1" r="1.8" fill="#111" />
          <path
            d="M-5 7.5 Q 0 12 5 7.5"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </g>
      </g>
    </g>
    <g class="art-tw" transform="translate(70 200)">
      <path d="M0 -12 L3 -3 L12 0 L3 3 L0 12 L-3 3 L-12 0 L-3 -3 Z" fill="#FFFFFF" />
    </g>
    <g class="art-tw2" transform="translate(455 205)">
      <path d="M0 -10 L2.5 -2.5 L10 0 L2.5 2.5 L0 10 L-2.5 2.5 L-10 0 L-2.5 -2.5 Z" fill="#111" />
    </g>
    <rect
      class="art-flash"
      x="-200"
      y="0"
      width="920"
      height="600"
      fill="#FFFFFF"
      pointer-events="none"
    />
  </svg>
</template>
