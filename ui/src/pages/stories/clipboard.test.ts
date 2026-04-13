import {copyToClipboard} from 'src/pages/stories/clipboard'
import {t} from 'src/i18n'
import {expect, test, vi} from 'vitest'

beforeEach(() => {
  (navigator as any).clipboard = {writeText: vi.fn()}
})

test('copy text content', async () => {
  const el = document.createElement('div')
  el.textContent = 'stuff to copy'
  await copyToClipboard({currentTarget: el} as any)
  expect(navigator.clipboard.writeText).toBeCalledWith('stuff to copy')
})

test('data-copy attribute', async () => {
  const el = document.createElement('div')
  el.textContent = 'visible text'
  el.dataset.copy = 'hidden text'
  await copyToClipboard({currentTarget: el} as any)
  expect(navigator.clipboard.writeText).toBeCalledWith('hidden text')
})

test('does not copy if content is already the "copied" message', async () => {
  const el = document.createElement('div')
  el.textContent = t.general.copied
  await copyToClipboard({currentTarget: el} as any)
  expect(navigator.clipboard.writeText).not.toHaveBeenCalled()
})
