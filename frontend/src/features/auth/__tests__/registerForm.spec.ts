import { describe, it, expect } from 'vitest'
import {
  emptyRegisterForm,
  mapRegisterServerErrors,
  toRegisterInput,
  validateRegisterForm,
} from '@/features/auth/registerForm'

const ok = { name: 'Анна', email: 'Anna@Mail.ru ', password: 'qwerty12', consent: true }

describe('validateRegisterForm', () => {
  it('пустая форма — все поля и согласие', () => {
    expect(Object.keys(validateRegisterForm(emptyRegisterForm())).sort()).toEqual(
      ['consent', 'email', 'name', 'password'].sort(),
    )
  })

  it('проверяет почту и длину пароля', () => {
    expect(validateRegisterForm({ ...ok, email: 'anna@' }).email).toBeDefined()
    expect(validateRegisterForm({ ...ok, password: '1234567' }).password).toBeDefined()
    expect(validateRegisterForm(ok)).toEqual({})
  })

  it('без согласия на обработку ПДн не регистрируем', () => {
    expect(validateRegisterForm({ ...ok, consent: false }).consent).toBeDefined()
  })
})

describe('toRegisterInput', () => {
  it('нормализует почту и отправляет согласие — бэк хранит его факт и время', () => {
    expect(toRegisterInput(ok)).toEqual({
      name: 'Анна',
      email: 'anna@mail.ru',
      password: 'qwerty12',
      consent: true,
    })
  })
})

describe('mapRegisterServerErrors', () => {
  it('берёт только известные поля', () => {
    expect(mapRegisterServerErrors({ email: 'Занят', other: 'x' })).toEqual({ email: 'Занят' })
  })
})
