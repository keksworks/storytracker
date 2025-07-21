import './shared/ArrayExtensions'
import './global.css'
import api from 'src/api/api'
import {initTranslations} from './i18n'
import {initErrorHandlers} from './errorHandlers'
import App from './App.svelte'
import {initSession} from 'src/stores/auth'
import type {User} from 'src/api/types'
import {mount} from 'svelte'

(async function() {
  initErrorHandlers()

  const [auth] = await Promise.all([api.get('user').catch(() => null), initTranslations()])
  if (auth) initSession(auth as User)

  document.body.innerHTML = ''
  mount(App, {target: document.body})
}())
