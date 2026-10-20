import { http } from './http'
import type { DemoAccount, Id, LoginResult, Me } from './types'

export const authApi = {
  demoAccounts: () => http.get<DemoAccount[]>('/auth/demo-accounts'),
  register: (body: { name: string; email: string; password: string; consent: boolean }) =>
    http.post<LoginResult>('/auth/register', body),
  login: (body: { email: string; password: string } | { demo_account_id: Id }) =>
    http.post<LoginResult>('/auth/login', body),
  logout: () => http.post<void>('/auth/logout'),
  me: () => http.get<Me>('/auth/me'),
}
