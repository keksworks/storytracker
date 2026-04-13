import {t} from 'src/i18n'

export async function copyToClipboard(e: Event) {
  const el = e.currentTarget as HTMLElement
  const text = el.textContent
  const v = el.dataset.copy || text
  if (!v || v === t.general.copied) return
  await navigator.clipboard.writeText(v)
  el.textContent = t.general.copied
  setTimeout(() => el.textContent = text, 1000)
}
