export interface RegisterFormState {
  name: string
  email: string
  password: string
  consent: boolean
}

export type RegisterFormErrors = Partial<Record<keyof RegisterFormState, string>>

export const PASSWORD_MIN = 8

export function emptyRegisterForm(): RegisterFormState {
  return { name: '', email: '', password: '', consent: false }
}

export function validateRegisterForm(form: RegisterFormState): RegisterFormErrors {
  const errors: RegisterFormErrors = {}
  const name = form.name.trim()
  if (!name) errors.name = 'Как к вам обращаться?'
  else if (name.length > 60) errors.name = 'Не длиннее 60 символов'
  const email = form.email.trim()
  if (!email) errors.email = 'Укажите почту'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
    errors.email = 'Проверьте адрес: например, anna@mail.ru'
  if (form.password.length < PASSWORD_MIN) errors.password = `Не короче ${PASSWORD_MIN} символов`
  if (!form.consent) errors.consent = 'Без согласия зарегистрироваться нельзя'
  return errors
}

// Бэк хранит факт и время согласия (152-ФЗ), поэтому согласие уходит вместе с регистрацией.
export function toRegisterInput(form: RegisterFormState) {
  return {
    name: form.name.trim(),
    email: form.email.trim().toLowerCase(),
    password: form.password,
    consent: form.consent,
  }
}

const KNOWN: (keyof RegisterFormState)[] = ['name', 'email', 'password', 'consent']

export function mapRegisterServerErrors(fields: Record<string, string>): RegisterFormErrors {
  const errors: RegisterFormErrors = {}
  for (const key of KNOWN) if (fields[key]) errors[key] = fields[key]
  return errors
}
