<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import type { Comment } from '@/api'
import { UiBadge } from '@/components/ui'
import { intlLocale } from '@/i18n'

const props = defineProps<{ comment: Comment; mine: boolean; canReply: boolean; busy?: boolean }>()
const emit = defineEmits<{ reply: []; save: [text: string]; remove: [] }>()

const editing = ref(false)
const draft = ref('')
const editId = useId()

const time = computed(() =>
  new Intl.DateTimeFormat(intlLocale(), {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(props.comment.created_at)),
)
const edited = computed(() => props.comment.is_edited)

function startEdit() {
  draft.value = props.comment.text ?? ''
  editing.value = true
}

function save() {
  const text = draft.value.trim()
  if (!text) return
  emit('save', text)
  editing.value = false
}
</script>

<template>
  <div v-if="comment.is_hidden" class="rounded-field px-3.5 py-2.5 text-ink-2">
    <p class="font-medium">{{ $t('comments.hidden') }}</p>
    <p v-if="comment.hidden_reason" class="text-sm">{{ comment.hidden_reason }}</p>
  </div>

  <div
    v-else
    class="flex flex-col gap-1 rounded-field px-3.5 py-2.5"
    :class="comment.is_organizer ? ' bg-accent-soft' : ' bg-card'"
  >
    <p class="flex flex-wrap items-center gap-x-2 gap-y-1">
      <span class="font-bold">{{ comment.as_organization?.name ?? comment.author.name }}</span>
      <UiBadge v-if="comment.is_organizer" tone="accent">{{ $t('event.organizer') }}</UiBadge>
      <span v-if="mine" class="text-sm text-ink-2">· {{ $t('comments.you') }}</span>
      <span class="text-sm text-ink-2">
        <time :datetime="comment.created_at">{{ time }}</time>
        <template v-if="edited"> · {{ $t('comments.edited') }}</template>
      </span>
    </p>

    <form v-if="editing" class="flex flex-col gap-2" @submit.prevent="save">
      <label :for="editId" class="sr-only">{{ $t('comments.text') }}</label>
      <textarea
        :id="editId"
        v-model="draft"
        rows="2"
        maxlength="1000"
        class="rounded-field bg-card p-2.5"
      />
      <div class="flex gap-4 text-sm font-bold">
        <button
          type="submit"
          class="inline-flex min-h-8 items-center underline underline-offset-4"
          :disabled="busy"
        >
          {{ $t('common.save') }}
        </button>
        <button
          type="button"
          class="inline-flex min-h-8 items-center underline underline-offset-4"
          @click="editing = false"
        >
          {{ $t('common.cancel') }}
        </button>
      </div>
    </form>
    <p v-else class="whitespace-pre-line">{{ comment.text }}</p>

    <div v-if="!editing && (canReply || mine)" class="flex gap-4 text-sm font-medium">
      <button
        v-if="canReply"
        type="button"
        class="inline-flex min-h-8 items-center underline underline-offset-4"
        @click="emit('reply')"
      >
        {{ $t('comments.reply') }}
      </button>
      <template v-if="mine">
        <button
          type="button"
          class="inline-flex min-h-8 items-center underline underline-offset-4"
          @click="startEdit"
        >
          {{ $t('common.edit') }}
        </button>
        <button
          type="button"
          class="inline-flex min-h-8 items-center underline underline-offset-4"
          :disabled="busy"
          @click="emit('remove')"
        >
          {{ $t('common.delete') }}
        </button>
      </template>
    </div>
  </div>
</template>
