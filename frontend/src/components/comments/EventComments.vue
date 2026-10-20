<script setup lang="ts">
import { computed, nextTick, ref, useId, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ApiError, commentsApi, type Comment, type EventDetails } from '@/api'
import { useSessionStore } from '@/stores/session'
import { UiButton } from '@/components/ui'
import { t } from '@/i18n'
import { buildThread } from '@/features/comments/thread'
import CommentItem from './CommentItem.vue'

const props = defineProps<{ event: EventDetails }>()

const session = useSessionStore()
const route = useRoute()

const comments = ref<Comment[]>([])
const state = ref<'loading' | 'ready' | 'error'>('loading')
const busy = ref(false)
const error = ref('')
const text = ref('')
const replyTo = ref<Comment | null>(null)
const inputId = useId()
const input = ref<HTMLTextAreaElement | null>(null)

const thread = computed(() => buildThread(comments.value))
const countText = computed(() => t('comments.count', comments.value.length))
const isOrganizer = computed(() => props.event.is_organizer)
// Владелец допущенной организации отвечает в обсуждении её события от её имени.
const asOrganizationId = computed(() =>
  isOrganizer.value && props.event.organizer.type === 'organization'
    ? props.event.organizer.id
    : undefined,
)
const placeholder = computed(() =>
  isOrganizer.value ? t('comments.organizerPlaceholder') : t('comments.placeholder'),
)

async function load() {
  state.value = 'loading'
  try {
    comments.value = (await commentsApi.list(props.event.id)).items
    state.value = 'ready'
  } catch {
    state.value = 'error'
  }
}

async function act(fn: () => Promise<unknown>) {
  busy.value = true
  error.value = ''
  try {
    await fn()
    comments.value = (await commentsApi.list(props.event.id)).items
    return true
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : t('comments.sendError')
    return false
  } finally {
    busy.value = false
  }
}

async function send() {
  const body = text.value.trim()
  if (!body) {
    error.value = t('comments.required')
    return
  }
  const ok = await act(() =>
    commentsApi.create(props.event.id, {
      text: body,
      parent_id: replyTo.value?.id,
      as_organization_id: asOrganizationId.value,
    }),
  )
  if (ok) {
    text.value = ''
    replyTo.value = null
  }
}

function saveComment(id: number, body: string) {
  return act(() => commentsApi.update(id, body))
}

function removeComment(id: number) {
  return act(() => commentsApi.remove(id))
}

async function reply(comment: Comment) {
  replyTo.value = comment
  await nextTick()
  input.value?.focus()
}

watch(() => props.event.id, load, { immediate: true })
</script>

<template>
  <section aria-labelledby="comments-title" class="flex flex-col gap-3">
    <h2 id="comments-title" class="flex items-baseline gap-2 text-lg font-bold">
      {{ $t('comments.title') }}
      <span v-if="state === 'ready'" class="text-base font-normal text-ink-2">{{ countText }}</span>
    </h2>

    <p v-if="state === 'loading'" class="text-ink-2" role="status">
      {{ $t('comments.loading') }}
    </p>
    <div v-else-if="state === 'error'" class="flex flex-col items-start gap-2" role="alert">
      <p class="text-ink-2">{{ $t('comments.loadError') }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">{{ $t('common.retry') }}</UiButton>
    </div>

    <template v-else>
      <p v-if="comments.length === 0" class="text-ink-2">
        {{ $t('comments.empty') }}
      </p>
      <ul class="flex flex-col gap-3">
        <li v-for="item in thread" :key="item.comment.id" class="flex flex-col gap-2">
          <CommentItem
            :comment="item.comment"
            :mine="item.comment.author.id === session.user?.id"
            :can-reply="!!session.user && event.status === 'published'"
            :busy="busy"
            @reply="reply(item.comment)"
            @save="(t: string) => saveComment(item.comment.id, t)"
            @remove="removeComment(item.comment.id)"
          />
          <ul v-if="item.replies.length" class="ml-5 flex flex-col gap-2 pl-3">
            <li v-for="r in item.replies" :key="r.id">
              <CommentItem
                :comment="r"
                :mine="r.author.id === session.user?.id"
                :can-reply="!!session.user && event.status === 'published'"
                :busy="busy"
                @reply="reply(item.comment)"
                @save="(t: string) => saveComment(r.id, t)"
                @remove="removeComment(r.id)"
              />
            </li>
          </ul>
        </li>
      </ul>

      <p v-if="event.status !== 'published'" class="text-ink-2">
        {{ $t('comments.closed') }}
      </p>
      <form v-else-if="session.user" class="flex flex-col gap-2" @submit.prevent="send">
        <p v-if="replyTo" class="flex items-center gap-3 text-sm">
          <span class="text-ink-2">
            {{
              $t('comments.replyTo', { name: replyTo.as_organization?.name ?? replyTo.author.name })
            }}
          </span>
          <button
            type="button"
            class="font-bold underline underline-offset-4"
            @click="replyTo = null"
          >
            {{ $t('comments.noReply') }}
          </button>
        </p>
        <label :for="inputId" class="sr-only">{{ placeholder }}</label>
        <div class="flex items-end gap-2">
          <textarea
            :id="inputId"
            ref="input"
            v-model="text"
            rows="2"
            maxlength="1000"
            :placeholder="placeholder"
            class="min-h-12.5 flex-1 rounded-field bg-card p-3 text-ink placeholder:text-ink-2"
            @input="error = ''"
          />
          <button
            type="submit"
            :disabled="busy"
            :aria-label="$t('comments.send')"
            class="grid size-12.5 flex-none place-items-center rounded-full bg-ink text-xl font-bold text-white disabled:bg-mute"
          >
            ↑
          </button>
        </div>
        <p v-if="asOrganizationId" class="text-sm text-ink-2">
          {{ $t('comments.asOrg') }}
        </p>
      </form>
      <p v-else>
        <RouterLink
          :to="{ name: 'login', query: { redirect: route.fullPath } }"
          class="font-bold text-accent-ink underline underline-offset-4"
        >
          {{ $t('comments.loginLink') }}</RouterLink
        >{{ $t('comments.loginRest') }}
      </p>
      <p v-if="error" class="font-medium text-accent-ink" role="alert">✕ {{ error }}</p>
    </template>
  </section>
</template>
