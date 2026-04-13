import {copyToClipboard} from 'src/pages/stories/clipboard'
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
