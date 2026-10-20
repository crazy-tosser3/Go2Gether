<script setup lang="ts">
import { ref } from 'vue'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import {
  StatusBadge,
  UiAvatarStack,
  UiBadge,
  UiButton,
  UiCard,
  UiCheckbox,
  UiChip,
  UiCombobox,
  UiIconButton,
  UiModal,
  UiSeats,
  UiSelect,
  UiTable,
  UiTextField,
  type UiOption,
} from '@/components/ui'

const query = ref('')
const email = ref('anna@')
const consent = ref(false)
const filter = ref('all')

const filters = [
  { id: 'all', label: 'Все', count: 24 },
  { id: 'today', label: 'Сегодня', count: 7 },
  { id: 'beginners', label: 'Для новичков', count: 11 },
]
const users = [
  { id: 1, name: 'Ольга Котова', avatar_url: null },
  { id: 2, name: 'Ирина Петрова', avatar_url: null },
  { id: 3, name: 'Анна Носова', avatar_url: null },
  { id: 4, name: 'Дмитрий Сидоров', avatar_url: null },
  { id: 5, name: 'Пётр Ли', avatar_url: null },
]
const sportOptions = ref<UiOption[]>([
  { value: 'football', label: 'Футбол' },
  { value: 'tennis', label: 'Теннис' },
  { value: 'chess', label: 'Шахматы' },
  { value: 'running', label: 'Бег' },
  { value: 'yoga', label: 'Йога' },
])
const sport = ref<string | null>('tennis')
function addSport(label: string) {
  const value = `custom-${sportOptions.value.length}`
  sportOptions.value.push({ value, label })
  sport.value = value
}
const level = ref('')
const levels: UiOption[] = [
  { value: 'beginner', label: 'Новичок' },
  { value: 'amateur', label: 'Любитель' },
  { value: 'pro', label: 'Опытный' },
]
const rejectOpen = ref(false)
const reason = ref('')
const orgColumns = [
  { key: 'name', label: 'Организация' },
  { key: 'type', label: 'Тип' },
  { key: 'created', label: 'Подана' },
  { key: 'status', label: 'Статус' },
]
const orgs = [
  {
    id: 'o1',
    name: 'Клуб «Шаг»',
    type: 'Клуб',
    created: 'сегодня, 10:12',
    status: 'pending' as const,
  },
  {
    id: 'o2',
    name: '«Мамы на пробежке»',
    type: 'Сообщество',
    created: 'вчера',
    status: 'pending' as const,
  },
  { id: 'o3', name: 'Клуб «Енисей»', type: 'Клуб', created: '2 окт', status: 'approved' as const },
]
const selectedOrg = ref<string | null>('o1')

const sports = [
  { icon: 'football', name: 'Футбол' },
  { icon: 'tennis', name: 'Теннис' },
  { icon: 'chess', name: 'Шахматы' },
  { icon: 'bike', name: 'Велосипед' },
  { icon: 'running', name: 'Бег' },
  { icon: null, name: 'Своё' },
]
</script>

<template>
  <main class="mx-auto flex max-w-5xl flex-col gap-10 px-4 py-10">
    <header>
      <p class="font-hand text-2xl text-accent-ink">витрина компонентов</p>
      <h1 class="text-[30px] lg:text-[42px]">UI-кит</h1>
    </header>

    <section class="flex flex-col gap-3">
      <h2 class="font-display text-xl font-medium">Кнопки</h2>
      <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <UiButton>Войти</UiButton>
        <UiButton variant="accent">Второстепенная</UiButton>
        <UiButton variant="ghost">Отменить</UiButton>
        <UiButton loading>Записаться</UiButton>
      </div>
      <div class="flex gap-3">
        <UiIconButton label="Меню">☰</UiIconButton>
        <UiIconButton label="Профиль" round filled>☺</UiIconButton>
      </div>
    </section>

    <section class="flex flex-col gap-3">
      <h2 class="font-display text-xl font-medium">Статусы</h2>
      <div class="flex flex-wrap gap-2">
        <StatusBadge :of="{ kind: 'participation', status: 'confirmed' }" />
        <StatusBadge :of="{ kind: 'participation', status: 'pending' }" />
        <StatusBadge :of="{ kind: 'participation', status: 'rejected' }" />
        <StatusBadge :of="{ kind: 'event', status: 'unpublished' }" />
        <StatusBadge :of="{ kind: 'organization', status: 'pending' }" />
        <UiBadge tone="accent">Организатор</UiBadge>
        <UiBadge>По заявке</UiBadge>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-6 md:grid-cols-2">
      <div class="flex flex-col gap-3">
        <h2 class="font-display text-xl font-medium">Поля</h2>
        <UiTextField
          v-model="query"
          label="Поиск"
          hide-label
          type="search"
          placeholder="Футбол, йога, шахматы…"
        />
        <UiTextField
          v-model="email"
          label="Почта"
          type="email"
          required
          error="Проверьте адрес: в нём нет домена"
        />
        <UiCheckbox v-model="consent">
          Даю согласие на обработку персональных данных по
          <RouterLink to="/privacy" class="text-accent-ink underline">политике</RouterLink>
        </UiCheckbox>
        <div class="flex gap-2 overflow-x-auto">
          <UiChip
            v-for="f in filters"
            :key="f.id"
            :count="f.count"
            :pressed="filter === f.id"
            @click="filter = f.id"
            >{{ f.label }}</UiChip
          >
        </div>
      </div>

      <div class="flex flex-col gap-3">
        <h2 class="font-display text-xl font-medium">Карточка события</h2>
        <UiCard as="article">
          <div class="flex items-center justify-between text-sm font-bold text-accent-ink">
            <span>Сегодня · 19:00</span>
            <UiBadge>По заявке</UiBadge>
          </div>
          <h3 class="font-display text-lg font-medium leading-tight">Теннис после работы</h3>
          <p class="text-[15px] text-ink-2">Стадион «Енисей», корт 3 · любой уровень</p>
          <UiSeats :taken="5" :capacity="8"><UiAvatarStack :users="users" /></UiSeats>
          <StatusBadge :of="{ kind: 'participation', status: 'pending' }" />
        </UiCard>
      </div>
    </section>

    <section class="grid grid-cols-1 gap-6 md:grid-cols-2">
      <div class="flex flex-col gap-3">
        <h2 class="font-display text-xl font-medium">Выбор из списка</h2>
        <UiCombobox
          v-model="sport"
          label="Направление"
          :options="sportOptions"
          allow-create
          hint="Нет нужного — впишите своё"
          @create="addSport"
        />
        <UiSelect v-model="level" label="Уровень" :options="levels" placeholder="Любой" />
      </div>
      <div class="flex flex-col gap-3">
        <h2 class="font-display text-xl font-medium">Модальное окно</h2>
        <p class="text-ink-2">Esc или клик по фону закрывают окно, фокус остаётся внутри.</p>
        <UiButton variant="ghost" @click="rejectOpen = true">Отклонить заявку…</UiButton>
        <UiModal
          v-model:open="rejectOpen"
          title="Отклонить заявку Анны?"
          description="Анна увидит причину в карточке и в «Моих записях»."
        >
          <UiTextField v-model="reason" label="Причина" required />
          <template #actions>
            <UiButton variant="ghost" @click="rejectOpen = false">Отмена</UiButton>
            <UiButton :disabled="!reason.trim()" @click="rejectOpen = false">Отклонить</UiButton>
          </template>
        </UiModal>
      </div>
    </section>

    <section class="flex flex-col gap-3">
      <h2 class="font-display text-xl font-medium">Таблица</h2>
      <UiTable
        caption="Организации"
        :columns="orgColumns"
        :rows="orgs"
        :row-key="(o) => o.id"
        :selected-key="selectedOrg"
        clickable
        @select="(o) => (selectedOrg = o.id)"
      >
        <template #cell-name="{ row }"
          ><b>{{ row.name }}</b></template
        >
        <template #cell-status="{ row }">
          <StatusBadge :of="{ kind: 'organization', status: row.status }" />
        </template>
      </UiTable>
      <UiTable
        caption="Пустая таблица"
        :columns="orgColumns"
        :rows="[]"
        :row-key="() => ''"
        empty-text="Заявок на допуск нет"
      />
    </section>

    <section class="flex flex-col gap-3">
      <h2 class="font-display text-xl font-medium">Направления</h2>
      <div class="grid grid-cols-3 gap-2 sm:grid-cols-6">
        <div
          v-for="s in sports"
          :key="s.name"
          class="flex flex-col items-center gap-1 rounded-md bg-card p-2.5 text-sm font-medium"
          :class="{ ' text-accent-ink': !s.icon }"
        >
          <SportIcon :name="s.icon" />
          {{ s.name }}
        </div>
      </div>
    </section>
  </main>
</template>
