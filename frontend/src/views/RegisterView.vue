<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError } from '@/api'
import OnboardingArt from '@/components/illustrations/OnboardingArt.vue'
import { UiButton, UiCheckbox, UiTextField } from '@/components/ui'
import {
  PASSWORD_MIN,
  emptyRegisterForm,
  mapRegisterServerErrors,
  toRegisterInput,
  validateRegisterForm,
  type RegisterFormErrors,
} from '@/features/auth/registerForm'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()
const route = useRoute()

const form = reactive(emptyRegisterForm())
const errors = ref<RegisterFormErrors>({})
const formError = ref('')
const busy = ref(false)

async function submit() {
  errors.value = validateRegisterForm(form)
  formError.value = ''
  if (Object.keys(errors.value).length) {
    await nextTick()
    document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    return
  }
  busy.value = true
  try {
    await session.register(toRegisterInput(form))
    // После регистрации — короткая настройка подбора; куда человек шёл, туда и вернём после неё.
    await router.replace({ name: 'onboarding', query: { redirect: route.query.redirect } })
  } catch (e) {
    if (e instanceof ApiError && e.status === 409) errors.value = { email: e.message }
    else if (e instanceof ApiError && Object.keys(e.fields).length)
      errors.value = mapRegisterServerErrors(e.fields)
    else formError.value = e instanceof ApiError ? e.message : 'Не получилось зарегистрироваться.'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section
    class="mx-auto grid max-w-md gap-4 xl:max-w-5xl xl:grid-cols-[minmax(0,1fr)_minmax(0,28rem)]"
  >
    <!-- Как в макете онбординга: слева — зачем регистрироваться, справа — форма. -->
    <aside
      class="relative hidden flex-col gap-4 overflow-hidden rounded-2xl bg-orange p-8 xl:flex"
      aria-hidden="true"
    >
      <p
        class="max-w-xs -rotate-3 self-start rounded-sm bg-cream px-3.5 py-1 font-hand text-2xl font-bold"
      >
        спорт — это когда вместе
      </p>
      <p class="text-4xl leading-tight font-bold text-danger">Найдите компанию для спорта!</p>
      <p class="max-w-md text-lg text-danger">
        Футбол во дворе, пробежка по набережной, шахматы в парке — собирайтесь с людьми своего
        уровня рядом с домом.
      </p>
      <OnboardingArt name="register" class="mt-auto" />
    </aside>

    <div class="flex flex-col gap-4">
      <header class="flex flex-col gap-1 rounded-card bg-accent p-6">
        <p class="font-hand text-2xl text-accent-ink xl:hidden">спорт — это когда вместе</p>
        <h1 class="text-[30px] lg:text-[42px]">Создайте аккаунт</h1>
        <p class="text-accent-ink">
          Дальше — пять коротких вопросов, и лента «Для вас» подстроится под вас.
        </p>
      </header>

      <form
        class="flex flex-col gap-4 rounded-card bg-card p-5"
        novalidate
        @submit.prevent="submit"
      >
        <UiTextField
          v-model="form.name"
          label="Имя"
          required
          placeholder="Анна"
          :error="errors.name"
        />
        <UiTextField
          v-model="form.email"
          label="Почта"
          type="email"
          required
          placeholder="anna@mail.ru"
          :error="errors.email"
        />
        <UiTextField
          v-model="form.password"
          label="Пароль"
          type="password"
          required
          :hint="`Не короче ${PASSWORD_MIN} символов`"
          :error="errors.password"
        />

        <div class="flex flex-col gap-1">
          <UiCheckbox v-model="form.consent">
            Даю согласие на обработку персональных данных по
            <RouterLink
              :to="{ name: 'privacy' }"
              class="font-semibold underline underline-offset-4"
            >
              политике
            </RouterLink>
          </UiCheckbox>
          <p v-if="errors.consent" class="ml-1 text-sm font-semibold text-danger" role="alert">
            ✕ {{ errors.consent }}
          </p>
        </div>

        <p v-if="formError" class="font-semibold text-danger" role="alert">✕ {{ formError }}</p>

        <UiButton type="submit" :disabled="!form.consent" :loading="busy">
          Создать аккаунт
        </UiButton>
        <p v-if="!form.consent" class="-mt-2 text-center text-sm text-ink-2">
          Кнопка станет активной после согласия
        </p>

        <p class="text-center">
          Уже есть аккаунт?
          <RouterLink :to="{ name: 'login' }" class="font-bold underline underline-offset-4">
            Войти
          </RouterLink>
        </p>
      </form>
    </div>
  </section>
</template>
