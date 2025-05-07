import {t} from 'src/i18n'

export async function copyToClipboard(e: Event) {
  const el = e.currentTarget as HTMLElement
  const v = el.textContent!
  await navigator.clipboard.writeText(v)
  el.textContent = t.general.copied
  setTimeout(() => el.textContent = v, 1000)
}
