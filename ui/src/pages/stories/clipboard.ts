import {t} from 'src/i18n'

export async function copyToClipboard(e: Event) {
  const el = e.currentTarget as HTMLElement
  const v = el.textContent!
  const textToCopy = el.getAttribute('data-copy-value') || el.textContent!
  if(el.textContent === t.general.copied) return;
  await navigator.clipboard.writeText(textToCopy)
  el.textContent = t.general.copied
  setTimeout(() => el.textContent = v, 1000)
}
