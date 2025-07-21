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


beforeEach(() => {
  user.set(testUser)
})

afterEach(() => {
  vi.restoreAllMocks()
})
