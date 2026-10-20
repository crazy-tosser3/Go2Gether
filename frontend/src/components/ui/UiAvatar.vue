<script setup lang="ts">
import { computed } from 'vue'
import AvatarArt from '@/components/illustrations/AvatarArt.vue'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { avatarView } from '@/features/profile/avatar'

const props = withDefaults(
  defineProps<{ name: string; url?: string | null; size?: 'sm' | 'md' | 'lg' | 'xl' }>(),
  { url: null, size: 'md' },
)

const SIZES = {
  sm: { box: 'size-8 text-[11px]', icon: 22 },
  md: { box: 'size-10 text-sm', icon: 28 },
  lg: { box: 'size-11', icon: 30 },
  xl: { box: 'size-20 text-2xl', icon: 56 },
}

const view = computed(() => avatarView(props.name, props.url))
const s = computed(() => SIZES[props.size])
</script>

<template>
  <span
    class="grid flex-none place-items-center overflow-hidden rounded-full font-bold text-ink"
    :class="[
      s.box,
      view.kind === 'preset' ? view.preset.bg : view.kind === 'initials' ? view.bg : 'bg-field',
    ]"
    aria-hidden="true"
  >
    <AvatarArt v-if="view.kind === 'preset' && view.preset.art" :kind="view.preset.art" />
    <SportIcon
      v-else-if="view.kind === 'preset' && view.preset.sport"
      :name="view.preset.sport"
      :size="s.icon"
    />
    <img v-else-if="view.kind === 'image'" :src="view.src" alt="" class="size-full object-cover" />
    <template v-else-if="view.kind === 'initials'">{{ view.text }}</template>
  </span>
</template>
