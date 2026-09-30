import './shared/ArrayExtensions'
import en from '../i18n/en.json'
import {initTestTranslations} from './i18n'
import {user} from 'src/stores/auth'
import {user as testUser} from 'src/api/types'

initTestTranslations('en', en)
window.fetch = () => new Promise(() => {})
window.scrollTo = () => {}

// not provided by jsdom
Element.prototype.animate = (() => ({
  cancel: () => {}
})) as any

if (!globalThis.localStorage) {
  const store: Record<string, string> = {}
  globalThis.localStorage = new Proxy(store, {
    get(target, prop) {
      if (prop === 'clear') return () => { for (const k in target) delete target[k] }
      if (prop === 'getItem') return (k: string) => target[k] ?? null
      if (prop === 'setItem') return (k: string, v: string) => { target[k] = String(v) }
      if (prop === 'removeItem') return (k: string) => { delete target[k] }
      if (prop === 'length') return Object.keys(target).length
      if (prop === 'key') return (i: number) => Object.keys(target)[i] ?? null
      return target[prop as string]
    },
    set(target, prop, value) {
      target[prop as string] = String(value)
      return true
    }
  }) as Storage
}


beforeEach(() => {
  user.set(testUser)
})

afterEach(() => {
  vi.restoreAllMocks()
})
