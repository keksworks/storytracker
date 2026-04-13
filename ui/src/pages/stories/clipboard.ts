import {t} from 'src/i18n'
import {showToast} from 'src/stores/toasts'

export async function copyToClipboard(e: Event) {
  const el = e.currentTarget as HTMLElement
  const v = el.dataset.copy || el.textContent
  if (!v) return
  await navigator.clipboard.writeText(v)
  showToast(t.general.copied)
}
