<script setup lang="ts">
import { computed, reactive } from 'vue'
import { RouterLink } from 'vue-router'
import type { OrganizationType } from '@/api'
import { UiButton, UiCheckbox, UiChip, UiTextArea, UiTextField } from '@/components/ui'
import { ORG_TYPE_LABELS } from '@/features/admin/organizations'
import type { OrgFormErrors, OrgFormState } from '@/features/organizations/orgForm'

const props = defineProps<{
  initial: OrgFormState
  errors: OrgFormErrors
  submitLabel: string
  busy: boolean
  /** При правке отклонённой заявки согласие уже дано — чекбокс не показываем. */
  askConsent: boolean
}>()
const emit = defineEmits<{ submit: [form: OrgFormState]; cancel: [] }>()

const form = reactive<OrgFormState>({ ...props.initial })
const types = computed(() => Object.entries(ORG_TYPE_LABELS) as [OrganizationType, string][])
</script>

<template>
  <form
    class="flex flex-col gap-4 rounded-card bg-card p-5"
    novalidate
    @submit.prevent="emit('submit', { ...form })"
  >
    <UiTextField
      v-model="form.name"
      label="Название"
      required
      placeholder="Клуб скандинавской ходьбы «Шаг»"
      :error="errors.name"
    />

    <fieldset class="flex flex-col gap-2">
      <legend class="mb-1.5 ml-1 text-[15px] font-semibold">
        Тип<span aria-hidden="true" class="text-orange"> *</span>
      </legend>
      <div
        class="flex flex-wrap gap-2"
        :class="errors.type ? 'rounded-full ring-2 ring-danger' : ''"
      >
        <UiChip
          v-for="[value, label] in types"
          :key="value"
          :pressed="form.type === value"
          @click="form.type = value"
        >
          {{ label }}
        </UiChip>
      </div>
      <p v-if="errors.type" class="ml-1 text-sm font-semibold text-danger" role="alert">
        ✕ {{ errors.type }}
      </p>
    </fieldset>

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <UiTextField
        v-model="form.sports"
        label="Направления"
        placeholder="Бег, ходьба"
        :error="errors.sports"
      />
      <UiTextField
        v-model="form.city"
        label="Город"
        placeholder="Красноярск"
        :error="errors.city"
      />
    </div>
    <UiTextField
      v-model="form.contacts"
      label="Контакты для участников"
      required
      placeholder="+7 900 000-00-00"
      :error="errors.contacts"
    />
    <UiTextArea
      v-model="form.description"
      label="О вас"
      :rows="3"
      :maxlength="1000"
      placeholder="Где и как часто занимаетесь"
      :error="errors.description"
    />
    <UiTextField
      v-model="form.website"
      label="Сайт или группа"
      placeholder="https://…"
      :error="errors.website"
    />

    <div v-if="askConsent" class="flex flex-col gap-1">
      <UiCheckbox v-model="form.consent">
        Даю согласие на обработку персональных данных по
        <RouterLink :to="{ name: 'privacy' }" class="font-semibold underline underline-offset-4">
          политике
        </RouterLink>
      </UiCheckbox>
      <p v-if="errors.consent" class="ml-1 text-sm font-semibold text-danger" role="alert">
        ✕ {{ errors.consent }}
      </p>
    </div>

    <div class="flex flex-col-reverse gap-2 sm:flex-row">
      <UiButton variant="ghost" @click="emit('cancel')">Отмена</UiButton>
      <UiButton type="submit" :loading="busy">{{ submitLabel }}</UiButton>
    </div>
  </form>
</template>
