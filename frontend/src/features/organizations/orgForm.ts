import type { Organization, OrganizationInput, OrganizationType } from '@/api'

export interface OrgFormState {
  name: string
  type: OrganizationType | ''
  sports: string
  city: string
  contacts: string
  description: string
  website: string
  consent: boolean
}

export type OrgFormErrors = Partial<Record<keyof OrgFormState, string>>

export function emptyOrgForm(): OrgFormState {
  return {
    name: '',
    type: '',
    sports: '',
    city: '',
    contacts: '',
    description: '',
    website: '',
    consent: false,
  }
}

/** Для исправления отклонённой заявки: согласие уже дано при первой подаче. */
export function orgFormFrom(org: Organization): OrgFormState {
  return {
    name: org.name,
    type: org.type,
    sports: org.sports_description ?? '',
    city: org.city ?? '',
    contacts: org.contacts ?? '',
    description: org.description ?? '',
    website: org.website_url ?? '',
    consent: true,
  }
}

export function validateOrgForm(form: OrgFormState): OrgFormErrors {
  const errors: OrgFormErrors = {}
  if (!form.name.trim()) errors.name = 'Укажите название'
  else if (form.name.trim().length > 120) errors.name = 'Не длиннее 120 символов'
  if (!form.type) errors.type = 'Выберите тип'
  if (!form.contacts.trim()) errors.contacts = 'Как с вами связаться — телефон или ссылка'
  const site = form.website.trim()
  if (site && !/^https?:\/\/\S+\.\S+/.test(site)) errors.website = 'Ссылка вида https://…'
  if (!form.consent) errors.consent = 'Без согласия отправить заявку нельзя'
  return errors
}

function optional(s: string): string | undefined {
  const t = s.trim()
  return t ? t : undefined
}

export function toOrgInput(form: OrgFormState): OrganizationInput {
  return {
    name: form.name.trim(),
    type: form.type as OrganizationType,
    contacts: form.contacts.trim(),
    sports_description: optional(form.sports),
    city: optional(form.city),
    description: optional(form.description),
    website_url: optional(form.website),
  }
}

const SERVER_FIELDS: Record<string, keyof OrgFormState> = {
  name: 'name',
  type: 'type',
  sports_description: 'sports',
  city: 'city',
  contacts: 'contacts',
  description: 'description',
  website_url: 'website',
}

export function mapOrgServerErrors(fields: Record<string, string>): OrgFormErrors {
  const errors: OrgFormErrors = {}
  for (const [key, message] of Object.entries(fields)) {
    const field = SERVER_FIELDS[key]
    if (field) errors[field] = message
  }
  return errors
}
