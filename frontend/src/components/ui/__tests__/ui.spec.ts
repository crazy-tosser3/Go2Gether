import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { plural, seatsLabel, seatsPercent, seatsShort, statusView } from '@/components/ui'
import StatusBadge from '@/components/ui/StatusBadge.vue'
import UiButton from '@/components/ui/UiButton.vue'
import UiChip from '@/components/ui/UiChip.vue'
import UiTextField from '@/components/ui/UiTextField.vue'

describe('statusView', () => {
  it('различает статусы записи тоном', () => {
    expect(statusView({ kind: 'participation', status: 'confirmed' })).toEqual({
      tone: 'ok',
      label: 'Вы участник',
    })
    expect(statusView({ kind: 'participation', status: 'pending' }).tone).toBe('wait')
    expect(statusView({ kind: 'participation', status: 'rejected' }).tone).toBe('no')
  })

  it('снятое и удалённое событие — неактивный тон', () => {
    expect(statusView({ kind: 'event', status: 'unpublished' }).tone).toBe('off')
    expect(statusView({ kind: 'event', status: 'deleted' }).tone).toBe('off')
  })

  it('организация на рассмотрении — ожидание', () => {
    expect(statusView({ kind: 'organization', status: 'pending' }).label).toBe('На рассмотрении')
  })
})

describe('места', () => {
  it('склоняет слова', () => {
    expect(plural(1, ['место', 'места', 'мест'])).toBe('место')
    expect(plural(3, ['место', 'места', 'мест'])).toBe('места')
    expect(plural(11, ['место', 'места', 'мест'])).toBe('мест')
    expect(plural(22, ['место', 'места', 'мест'])).toBe('места')
  })

  it('считает заполненность и подпись', () => {
    expect(seatsPercent(5, 8)).toBe(63)
    expect(seatsPercent(10, 8)).toBe(100)
    expect(seatsPercent(3, null)).toBe(0)
    expect(seatsLabel(5, 8)).toBe('5 из 8 · осталось 3 места')
    expect(seatsLabel(8, 8)).toBe('8 из 8 · мест нет')
    expect(seatsLabel(2, null)).toBe('2 участника')
    expect(seatsShort(5, 8)).toBe('ещё 3 места')
    expect(seatsShort(8, 8)).toBe('мест нет')
    expect(seatsShort(1, null)).toBe('1 участник')
  })
})

describe('компоненты', () => {
  it('StatusBadge показывает значок и подпись', () => {
    const w = mount(StatusBadge, { props: { of: { kind: 'participation', status: 'confirmed' } } })
    expect(w.text()).toContain('✓')
    expect(w.text()).toContain('Вы участник')
  })

  it('StatusBadge принимает свою подпись', () => {
    const w = mount(StatusBadge, {
      props: { of: { kind: 'participation', status: 'confirmed' }, label: 'Участник' },
    })
    expect(w.text()).toContain('Участник')
    expect(w.text()).not.toContain('Вы участник')
  })

  it('UiButton блокируется при загрузке', () => {
    const w = mount(UiButton, { props: { loading: true }, slots: { default: 'Записаться' } })
    expect(w.attributes('disabled')).toBeDefined()
    expect(w.text()).toContain('Подождите…')
  })

  it('UiChip сообщает нажатие и состояние', async () => {
    const w = mount(UiChip, { props: { pressed: true, count: 7 }, slots: { default: 'Сегодня' } })
    expect(w.attributes('aria-pressed')).toBe('true')
    await w.trigger('click')
    expect(w.emitted('click')).toHaveLength(1)
  })

  it('UiTextField связывает подпись, значение и ошибку', async () => {
    const w = mount(UiTextField, {
      props: { label: 'Почта', modelValue: '', error: 'Нет домена' },
    })
    const input = w.get('input')
    expect(w.get('label').attributes('for')).toBe(input.attributes('id'))
    expect(input.attributes('aria-invalid')).toBe('true')
    await input.setValue('a@b.ru')
    expect(w.emitted('update:modelValue')?.[0]).toEqual(['a@b.ru'])
  })
})
