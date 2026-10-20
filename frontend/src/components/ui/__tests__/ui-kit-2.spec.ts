import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { creatableLabel, filterOptions, normalize } from '@/components/ui'
import UiCombobox from '@/components/ui/UiCombobox.vue'
import UiTable from '@/components/ui/UiTable.vue'

const sports = [
  { value: 'football', label: 'Футбол' },
  { value: 'tennis', label: 'Теннис' },
  { value: 'table-tennis', label: 'Настольный теннис' },
  { value: 'chess', label: 'Шахматы' },
]

describe('поиск по вариантам', () => {
  it('не различает регистр, «ё» и лишние пробелы', () => {
    expect(normalize('  Ёлка  в  ПАРКЕ ')).toBe('елка в парке')
  })

  it('сначала совпадения с начала, потом по вхождению', () => {
    expect(filterOptions(sports, 'тен').map((o) => o.value)).toEqual(['tennis', 'table-tennis'])
    expect(filterOptions(sports, '').length).toBe(4)
  })

  it('предлагает своё направление, только если такого нет', () => {
    expect(creatableLabel(sports, '  скандинавская   ходьба ')).toBe('скандинавская ходьба')
    expect(creatableLabel(sports, 'теннис')).toBeNull()
    expect(creatableLabel(sports, '  ')).toBeNull()
  })
})

describe('UiCombobox', () => {
  it('выбирает вариант с клавиатуры', async () => {
    const w = mount(UiCombobox, {
      props: { label: 'Направление', options: sports, modelValue: null },
    })
    const input = w.get('input')
    await input.setValue('тен')
    await input.trigger('keydown', { key: 'ArrowDown' })
    await input.trigger('keydown', { key: 'Enter' })
    expect(w.emitted('update:modelValue')?.slice(-1)[0]).toEqual(['table-tennis'])
  })

  it('предлагает добавить своё и сообщает об этом', async () => {
    const w = mount(UiCombobox, {
      props: { label: 'Направление', options: sports, modelValue: null, allowCreate: true },
    })
    const input = w.get('input')
    await input.setValue('Сап-серфинг')
    expect(w.text()).toContain('Добавить «Сап-серфинг»')
    await input.trigger('keydown', { key: 'Enter' })
    expect(w.emitted('create')).toEqual([['Сап-серфинг']])
  })

  it('Esc закрывает список и возвращает выбранное значение', async () => {
    const w = mount(UiCombobox, {
      props: { label: 'Направление', options: sports, modelValue: 'chess' },
    })
    const input = w.get('input')
    await input.setValue('фут')
    expect(input.attributes('aria-expanded')).toBe('true')
    await input.trigger('keydown', { key: 'Escape' })
    expect(input.attributes('aria-expanded')).toBe('false')
    expect((input.element as HTMLInputElement).value).toBe('Шахматы')
  })
})

describe('UiTable', () => {
  const columns = [
    { key: 'name', label: 'Название' },
    { key: 'city', label: 'Город' },
  ]
  const rows = [
    { id: '1', name: 'Клуб «Енисей»', city: 'Красноярск' },
    { id: '2', name: '«Ладья»', city: 'Красноярск' },
  ]

  it('показывает пустое состояние', () => {
    const w = mount(UiTable, {
      props: { caption: 'Орг', columns, rows: [], rowKey: () => '', emptyText: 'Заявок нет' },
    })
    expect(w.text()).toContain('Заявок нет')
  })

  it('отмечает выбранную строку и сообщает о клике', async () => {
    const w = mount(UiTable, {
      props: {
        caption: 'Орг',
        columns,
        rows,
        rowKey: (r: unknown) => (r as (typeof rows)[number]).id,
        selectedKey: '2',
        clickable: true,
      },
    })
    const trs = w.findAll('tbody tr')
    expect(trs[1]!.attributes('aria-current')).toBe('true')
    await trs[0]!.trigger('click')
    expect(w.emitted('select')?.[0]).toEqual([rows[0]])
  })
})
