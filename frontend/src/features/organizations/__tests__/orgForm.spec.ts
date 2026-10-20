import { describe, it, expect } from 'vitest'
import type { Organization } from '@/api'
import {
  emptyOrgForm,
  mapOrgServerErrors,
  orgFormFrom,
  toOrgInput,
  validateOrgForm,
  type OrgFormState,
} from '@/features/organizations/orgForm'

const filled = (patch: Partial<OrgFormState> = {}): OrgFormState => ({
  ...emptyOrgForm(),
  name: '  Клуб «Шаг» ',
  type: 'club',
  contacts: '+7 900 000-00-00',
  consent: true,
  ...patch,
})

describe('validateOrgForm', () => {
  it('пустая форма — название, тип, контакты и согласие', () => {
    expect(Object.keys(validateOrgForm(emptyOrgForm())).sort()).toEqual(
      ['consent', 'contacts', 'name', 'type'].sort(),
    )
  })

  it('без согласия на обработку ПДн не отправляем', () => {
    expect(validateOrgForm(filled({ consent: false })).consent).toBeDefined()
  })

  it('сайт — только ссылка', () => {
    expect(validateOrgForm(filled({ website: 'shag' })).website).toBeDefined()
    expect(validateOrgForm(filled({ website: 'https://shag.example' }))).toEqual({})
  })
})

describe('toOrgInput', () => {
  it('обрезает пробелы и не шлёт пустые поля', () => {
    const input = toOrgInput(filled({ city: '  ' }))
    expect(input.name).toBe('Клуб «Шаг»')
    expect(input.city).toBeUndefined()
    expect(input.type).toBe('club')
  })
})

describe('orgFormFrom', () => {
  it('подставляет данные отклонённой заявки и считает согласие данным', () => {
    const form = orgFormFrom({
      name: 'Шаг',
      type: 'community',
      sports_description: 'Ходьба',
      city: null,
      contacts: 'tg',
      description: null,
      website_url: null,
    } as Organization)
    expect(form.sports).toBe('Ходьба')
    expect(form.city).toBe('')
    expect(form.consent).toBe(true)
  })
})

describe('mapOrgServerErrors', () => {
  it('переносит ошибки сервера на поля формы', () => {
    expect(mapOrgServerErrors({ sports_description: 'Слишком длинно', x: 'y' })).toEqual({
      sports: 'Слишком длинно',
    })
  })
})
