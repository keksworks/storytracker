import {linkify} from './linkify'

describe('linkify', () => {
  it('prefer newlines to <br>', () => {
    expect(linkify('Hello<br>World')).toBe('Hello\nWorld')
  })

  it('prefer newlines to <div>s', () => {
    expect(linkify('Hello<div><div>World</div></div>')).toBe('Hello\n\nWorld')
  })

  it('linkify plain text url', () => {
    expect(linkify('Check https://google.com')).toBe('Check <a href="https://google.com">https://google.com</a>')
  })

  it('linkify multiple urls', () => {
    expect(linkify('Go to http://a.com and http://b.com')).toContain('http://a.com')
    expect(linkify('Go to http://a.com and http://b.com')).toContain('http://b.com')
  })

  it('do not linkify already linked urls', () => {
    const html = '<a href="https://google.com">https://google.com</a>'
    expect(linkify(html)).toBe(html)
  })

  it('linkify text around existing links', () => {
    const html = 'See http://a.com and <a href="http://b.com">http://b.com</a>'
    const result = linkify(html)
    expect(result).toContain('href="http://a.com"')
    expect(result).toContain('<a href="http://b.com">')
  })

  it('remove script and style tags', () => {
    const html = 'Hello <script>alert(1)</script><style>body{color:red}</style>world'
    expect(linkify(html)).toBe('Hello world')
  })

  it('handle empty or null input', () => {
    expect(linkify('')).toBe('')
    expect(linkify(null as any)).toBe(null)
  })
})
