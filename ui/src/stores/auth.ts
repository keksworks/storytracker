import {writable} from 'svelte/store'
import type {User} from 'src/api/types'
import api from 'src/api/api'

export const user = writable<User>()

export function initSession(auth: User) {
  user.set(auth)
}

export async function logout() {
  await api.get('logout')
  setTimeout(() => location.href = '/', 0)
}
