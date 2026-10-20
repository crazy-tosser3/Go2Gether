<script setup lang="ts">
import { useId } from 'vue'
import { LOCALES, useLocale } from '@/i18n'

const locale = useLocale()
// Как у темы: переключатель есть и в шапке, и в мобильном меню — у каждой группы своё имя.
const name = `locale-${useId()}`
</script>

<template>
  <fieldset class="flex-none">
    <legend class="sr-only">{{ $t('locale.title') }}</legend>
    <div class="flex gap-0.5 rounded-full bg-field p-[3px]">
      <label
        v-for="o in LOCALES"
        :key="o.value"
        :lang="o.value"
        :title="o.label"
        class="grid h-9 min-w-9 cursor-pointer place-items-center rounded-full px-2 font-display text-[13px] text-ink-2 uppercase hover:text-ink has-checked:bg-ink has-checked:text-white has-checked:shadow-[0_2px_6px_rgb(0_0_0/0.25)] has-focus-visible:outline-3 has-focus-visible:outline-accent-ink"
      >
        <input v-model="locale" type="radio" :name="name" :value="o.value" class="sr-only" />
        <span class="sr-only">{{ o.label }}</span>
        <span aria-hidden="true">{{ o.value }}</span>
      </label>
    </div>
  </fieldset>
</template>
