import {expect, test} from 'vitest'
import {linkify} from './linkify'

test('linkify plain text url', () => {
  expect(linkify('Check https://google.com')).toBe('Check <a href="https://google.com" target="_blank" class="text-blue-600 hover:underline">https://google.com</a>')
})

test('linkify multiple urls', () => {
  expect(linkify('Go to http://a.com and http://b.com')).toContain('http://a.com')
  expect(linkify('Go to http://a.com and http://b.com')).toContain('http://b.com')
})

test('do not linkify already linked urls', () => {
  const html = '<a href="https://google.com">https://google.com</a>'
  expect(linkify(html)).toBe(html)
})

test('linkify text around existing links', () => {
  const html = 'See http://a.com and <a href="http://b.com">http://b.com</a>'
  const result = linkify(html)
  expect(result).toContain('href="http://a.com"')
  expect(result).toContain('<a href="http://b.com">')
})

test('remove script and style tags', () => {
  const html = 'Hello <script>alert(1)</script><style>body{color:red}</style>world'
  expect(linkify(html)).toBe('Hello world')
})

test('handle empty or null input', () => {
  expect(linkify('')).toBe('')
  expect(linkify(null as any)).toBe(null)
})
