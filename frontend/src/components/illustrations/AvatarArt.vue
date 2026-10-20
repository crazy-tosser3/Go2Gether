<script setup lang="ts">
import type { AvatarArtId } from '@/features/profile/avatar'

defineProps<{ kind: AvatarArtId }>()
</script>

<!-- Аватары из макета Go2Gether: площадки сверху, мячи в прыжке, цветные блоки, неоновый силуэт. -->
<template>
  <svg viewBox="0 0 100 100" class="block size-full" aria-hidden="true">
    <template v-if="kind === 'pool'">
      <rect width="100" height="100" fill="#A5C8FF" />
      <g transform="rotate(-30 50 50)">
        <rect x="-20" y="-20" width="140" height="22" fill="#C9D3FF" />
        <rect x="-20" y="78" width="140" height="42" fill="#C9D3FF" />
        <path
          d="M-20 2 H120 M-20 78 H120"
          stroke="#FFFFFF"
          stroke-width="3"
          stroke-dasharray="7 5"
        />
        <path
          d="M0 40 H30 M8 58 H34"
          stroke="#FFFFFF"
          stroke-opacity="0.7"
          stroke-width="2"
          stroke-linecap="round"
        />
        <g class="art-drift">
          <ellipse cx="56" cy="54" rx="22" ry="5" fill="#1B1F6B" fill-opacity="0.18" />
          <path
            d="M28 46 L46 48 M28 54 L46 52"
            stroke="#E0A27C"
            stroke-width="4"
            stroke-linecap="round"
          />
          <ellipse cx="56" cy="50" rx="11" ry="6" fill="#111" />
          <path
            d="M66 50 L84 46 M66 50 L84 55"
            stroke="#E0A27C"
            stroke-width="4"
            stroke-linecap="round"
          />
          <circle cx="44" cy="50" r="5.5" fill="#F28C28" />
        </g>
      </g>
    </template>
    <template v-if="kind === 'court'">
      <rect width="100" height="100" fill="#FFE0B0" />
      <g transform="rotate(-25 50 50)">
        <rect x="-20" y="-20" width="140" height="30" fill="#A5B4FF" />
        <path d="M-20 10 H120" stroke="#FFFFFF" stroke-width="3" />
        <path d="M30 10 A28 28 0 0 0 86 10 Z" fill="#F28C28" />
        <path d="M30 10 A28 28 0 0 0 86 10" fill="none" stroke="#1B1F6B" stroke-width="3" />
        <path d="M10 10 A48 48 0 0 0 106 10" fill="none" stroke="#1B1F6B" stroke-width="5" />
        <g class="art-drift2">
          <ellipse cx="44" cy="70" rx="13" ry="7" fill="#111" fill-opacity="0.18" />
          <ellipse cx="40" cy="64" rx="13" ry="7" fill="#FFFFFF" />
          <circle cx="40" cy="64" r="5.5" fill="#111" />
          <circle cx="54" cy="70" r="4.5" fill="#F28C28" stroke="#111" stroke-width="1" />
        </g>
      </g>
    </template>
    <template v-if="kind === 'track'">
      <rect width="100" height="100" fill="#F28C28" />
      <g transform="rotate(-30 50 50)">
        <rect x="-20" y="18" width="140" height="64" fill="#F6A55A" />
        <path
          d="M-20 18 H120 M-20 39 H120 M-20 61 H120 M-20 82 H120"
          stroke="#FFF7EC"
          stroke-width="2.5"
        />
        <g class="art-run">
          <ellipse cx="54" cy="56" rx="22" ry="6" fill="#111" fill-opacity="0.2" />
          <path d="M30 50 H72" stroke="#111" stroke-width="3" stroke-linecap="round" />
          <circle cx="32" cy="50" r="3" fill="#111" />
          <circle cx="70" cy="50" r="3" fill="#111" />
          <ellipse cx="50" cy="50" rx="7" ry="10" fill="#1B1F6B" />
          <path
            d="M54 44 L62 46 M54 56 L62 54"
            stroke="#1B1F6B"
            stroke-width="3"
            stroke-linecap="round"
          />
          <circle cx="56" cy="50" r="5" fill="#A5B4FF" stroke="#111" stroke-width="1.5" />
        </g>
      </g>
    </template>
    <template v-if="kind === 'tennisCourt'">
      <rect width="100" height="100" fill="#F4B6C2" />
      <g transform="rotate(-30 50 50)">
        <rect x="-20" y="12" width="140" height="76" fill="#F7C7D1" />
        <path
          d="M-20 12 H120 M-20 88 H120 M-20 26 H120 M-20 74 H120 M60 26 V74 M86 -10 V110"
          stroke="#FFFFFF"
          stroke-width="2.5"
        />
        <path d="M86 -10 V110" stroke="#111" stroke-width="1.5" stroke-dasharray="2 2" />
        <g class="art-drift">
          <ellipse cx="38" cy="56" rx="11" ry="6" fill="#111" fill-opacity="0.18" />
          <ellipse cx="34" cy="50" rx="11" ry="6" fill="#1B1F6B" />
          <path d="M44 48 L54 40" stroke="#E0A27C" stroke-width="3.5" stroke-linecap="round" />
          <ellipse
            cx="58"
            cy="35"
            rx="5"
            ry="7"
            fill="none"
            stroke="#111"
            stroke-width="2"
            transform="rotate(40 58 35)"
          />
          <circle cx="34" cy="50" r="5" fill="#111" />
          <circle cx="72" cy="30" r="3" fill="#DDF46B" stroke="#111" stroke-width="0.8" />
        </g>
      </g>
    </template>
    <template v-if="kind === 'pitch'">
      <rect width="100" height="100" fill="#8FD39A" />
      <g transform="rotate(-30 50 50)">
        <rect x="-20" y="-20" width="20" height="140" fill="#9EDCA8" />
        <rect x="20" y="-20" width="20" height="140" fill="#9EDCA8" />
        <rect x="60" y="-20" width="20" height="140" fill="#9EDCA8" />
        <rect x="100" y="-20" width="20" height="140" fill="#9EDCA8" />
        <path d="M50 -20 V120" stroke="#FFFFFF" stroke-width="2.5" />
        <circle cx="50" cy="50" r="22" fill="none" stroke="#FFFFFF" stroke-width="2.5" />
        <g class="art-drift2">
          <ellipse cx="36" cy="64" rx="11" ry="6" fill="#111" fill-opacity="0.2" />
          <ellipse cx="32" cy="58" rx="11" ry="6" fill="#F28C28" />
          <circle cx="32" cy="58" r="5" fill="#111" />
          <circle cx="46" cy="66" r="4" fill="#FFFFFF" stroke="#111" stroke-width="1.2" />
        </g>
      </g>
    </template>
    <template v-if="kind === 'runners'">
      <rect width="100" height="100" fill="#E9B5F5" />
      <path d="M-10 110 A90 90 0 0 1 110 -10" fill="none" stroke="#F28C28" stroke-width="44" />
      <path
        d="M-10 110 A90 90 0 0 1 110 -10"
        fill="none"
        stroke="#FFF7EC"
        stroke-width="2"
        stroke-dasharray="6 5"
        class="art-flow"
      />
      <path
        d="M2 110 A100 100 0 0 1 110 2 M-22 110 A80 80 0 0 1 110 -22"
        fill="none"
        stroke="#FFF7EC"
        stroke-width="2"
      />
      <g class="art-run">
        <ellipse
          cx="44"
          cy="50"
          rx="9"
          ry="5"
          fill="#111"
          fill-opacity="0.2"
          transform="rotate(-45 44 50)"
        />
        <ellipse cx="40" cy="46" rx="9" ry="5" fill="#FFFFFF" transform="rotate(-45 40 46)" />
        <circle cx="40" cy="46" r="4.5" fill="#111" />
      </g>
      <g class="art-run2">
        <ellipse
          cx="60"
          cy="66"
          rx="9"
          ry="5"
          fill="#111"
          fill-opacity="0.2"
          transform="rotate(-45 60 66)"
        />
        <ellipse cx="56" cy="62" rx="9" ry="5" fill="#1B1F6B" transform="rotate(-45 56 62)" />
        <circle cx="56" cy="62" r="4.5" fill="#111" />
      </g>
    </template>
    <template v-if="kind === 'basketball'">
      <rect width="100" height="100" fill="#2B2F7A" />
      <ellipse class="art-zoom" cx="50" cy="80" rx="14" ry="3" fill="#111" fill-opacity="0.5" />
      <g class="art-jump">
        <g transform="translate(50 42)">
          <circle r="18" fill="#F07AA8" />
          <path
            d="M-18 0 H18 M0 -18 V18 M-12 -13 Q -4 0 -12 13 M12 -13 Q 4 0 12 13"
            fill="none"
            stroke="#B3336B"
            stroke-width="2"
          />
          <path
            d="M-10 -12 A14 14 0 0 1 4 -15"
            fill="none"
            stroke="#FFFFFF"
            stroke-opacity="0.6"
            stroke-width="2.5"
            stroke-linecap="round"
          />
        </g>
      </g>
    </template>
    <template v-if="kind === 'tennisBall'">
      <rect width="100" height="100" fill="#A5B4FF" />
      <ellipse class="art-zoom" cx="50" cy="80" rx="13" ry="3" fill="#1B1F6B" fill-opacity="0.35" />
      <g class="art-jump2">
        <g transform="translate(50 44)">
          <circle r="16" fill="#DDF46B" />
          <path
            d="M-14 -8 Q 0 0 -14 8 M14 -8 Q 0 0 14 8"
            fill="none"
            stroke="#FFFFFF"
            stroke-width="2.5"
          />
          <circle r="16" fill="none" stroke="#111" stroke-width="1.5" />
        </g>
      </g>
    </template>
    <template v-if="kind === 'volleyball'">
      <rect width="100" height="100" fill="#F28C28" />
      <ellipse class="art-zoom" cx="50" cy="80" rx="14" ry="3" fill="#111" fill-opacity="0.35" />
      <g class="art-jump">
        <g transform="translate(50 42)">
          <g class="art-spin">
            <circle r="18" fill="#FFFFFF" stroke="#111" stroke-width="1.5" />
            <path
              d="M0 0 Q 4 -12 -4 -18 M0 0 Q -12 2 -16 10 M0 0 Q 12 4 14 14"
              fill="none"
              stroke="#1B1F6B"
              stroke-width="2.2"
            />
            <path d="M0 -18 Q 14 -8 18 4" fill="none" stroke="#1B1F6B" stroke-width="2.2" />
          </g>
        </g>
      </g>
    </template>
    <template v-if="kind === 'blocks'">
      <rect width="100" height="100" fill="#F5C64A" />
      <path d="M0 0 H40 L22 100 H0 Z" fill="#FFF7EC" />
      <path d="M0 70 L100 58 V100 H0 Z" fill="#E2432B" />
      <path d="M60 0 H100 V40 Z" fill="#1B1F6B" />
      <g class="art-drift">
        <g transform="translate(58 34)">
          <circle r="15" fill="#E2432B" />
          <path d="M-15 0 A15 15 0 0 0 15 0 Z" fill="#111" />
          <path
            d="M-11 -10 Q 0 0 -11 10 M11 -10 Q 0 0 11 10"
            fill="none"
            stroke="#111"
            stroke-width="1.8"
          />
        </g>
      </g>
    </template>
    <template v-if="kind === 'neon'">
      <rect width="100" height="100" fill="#2F3CF5" />
      <path d="M44 84 L96 100 L60 100 Z" fill="#111" fill-opacity="0.45" />
      <g class="art-run" transform="translate(52 22) scale(0.3)">
        <g fill="none" stroke-linecap="round" stroke-linejoin="round">
          <path
            d="M4 52 L-30 84 L-58 78 M0 128 L-44 176 L-96 168 M10 46 L2 118 M4 132 L52 168 L36 236 M10 54 L46 86 L76 58"
            stroke="#F07AA8"
            stroke-width="34"
          />
          <path
            d="M4 52 L-30 84 L-58 78 M0 128 L-44 176 L-96 168 M10 46 L2 118 M4 132 L52 168 L36 236 M10 54 L46 86 L76 58"
            stroke="#3B2FA8"
            stroke-width="24"
          />
          <path d="M10 46 L2 118" stroke="#5B4FE0" stroke-width="40" />
        </g>
        <circle cx="18" cy="8" r="26" fill="#F07AA8" />
        <circle cx="18" cy="8" r="21" fill="#3B2FA8" />
      </g>
    </template>
    <template v-if="kind === 'poster'">
      <rect width="100" height="100" fill="#111" />
      <path d="M0 0 H100 V46 L0 64 Z" fill="#E9B5F5" />
      <path d="M0 64 L100 46 V58 L0 76 Z" fill="#FFFFFF" />
      <circle cx="76" cy="76" r="30" fill="#F28C28" />
      <g class="art-drift" transform="rotate(-35 40 40)">
        <ellipse cx="40" cy="30" rx="13" ry="17" fill="none" stroke="#111" stroke-width="4" />
        <path
          d="M30 22 H50 M28 30 H52 M30 38 H50 M34 16 V44 M40 13 V47 M46 16 V44"
          stroke="#111"
          stroke-width="1.2"
        />
        <path d="M40 47 V68" stroke="#111" stroke-width="6" stroke-linecap="round" />
      </g>
      <circle class="art-tw" cx="72" cy="22" r="5" fill="#DDF46B" stroke="#111" stroke-width="1" />
    </template>
  </svg>
</template>
