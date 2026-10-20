<script setup lang="ts">
import { computed, ref } from 'vue'
import { ApiError, eventsApi, type EventDetails, type ReportReason } from '@/api'
import { UiButton, UiModal, UiTextArea } from '@/components/ui'
import { t } from '@/i18n'
import {
  COMMENT_MAX,
  REPORT_REASONS,
  canReport,
  reasonLabel,
  reportError,
} from '@/features/reports/reports'
import { useSessionStore } from '@/stores/session'

const props = defineProps<{ event: EventDetails }>()

const session = useSessionStore()
const visible = computed(() => canReport(props.event, session.user))

const open = ref(false)
const reason = ref<ReportReason | null>(null)
const comment = ref('')
const errors = ref<{ reason?: string; comment?: string }>({})
const busy = ref(false)
const sent = ref(false)
const failure = ref('')

function start() {
  reason.value = null
  comment.value = ''
  errors.value = {}
  failure.value = ''
  open.value = true
}

async function send() {
  errors.value = reportError(reason.value, comment.value)
  if (Object.keys(errors.value).length) return
  busy.value = true
  failure.value = ''
  try {
    await eventsApi.report(props.event.id, {
      reason: reason.value!,
      comment: comment.value.trim() || undefined,
    })
    open.value = false
    sent.value = true
  } catch (e) {
    if (e instanceof ApiError && e.fields.comment) errors.value = { comment: e.fields.comment }
    else failure.value = e instanceof ApiError ? e.message : t('reports.sendFailed')
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section v-if="visible" class="px-1">
    <p v-if="sent" class="text-sm font-semibold" role="status">{{ $t('reports.sent') }}</p>
    <button
      v-else
      type="button"
      class="inline-flex min-h-11 items-center text-sm font-semibold text-ink-2 underline underline-offset-4 hover:text-ink"
      @click="start"
    >
      {{ $t('reports.open') }}
    </button>

    <UiModal v-model:open="open" :title="$t('reports.title')" :description="$t('reports.text')">
      <fieldset class="flex flex-col gap-1.5">
        <legend class="mb-1.5 ml-1 text-[15px] font-semibold">{{ $t('reports.reason') }}</legend>
        <label
          v-for="r in REPORT_REASONS"
          :key="r"
          class="flex min-h-11 cursor-pointer items-center gap-3 rounded-lg px-3 hover:bg-field has-checked:bg-field has-focus-visible:outline-3 has-focus-visible:outline-accent-ink"
        >
          <input
            v-model="reason"
            type="radio"
            name="report-reason"
            :value="r"
            class="size-5 accent-ink"
            :aria-invalid="errors.reason ? 'true' : undefined"
          />
          {{ reasonLabel(r) }}
        </label>
        <p v-if="errors.reason" class="ml-1 text-sm font-semibold text-danger" role="alert">
          ✕ {{ errors.reason }}
        </p>
      </fieldset>
      <UiTextArea
        v-model="comment"
        :label="reason === 'other' ? $t('reports.commentRequired') : $t('reports.comment')"
        :rows="3"
        :maxlength="COMMENT_MAX"
        :placeholder="$t('reports.commentPlaceholder')"
        :error="errors.comment"
      />
      <p v-if="failure" class="font-semibold text-danger" role="alert">✕ {{ failure }}</p>
      <template #actions>
        <UiButton variant="ghost" @click="open = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton :loading="busy" @click="send">{{ $t('reports.send') }}</UiButton>
      </template>
    </UiModal>
  </section>
</template>
