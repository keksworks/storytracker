const REF_REGEX = /(https?:\/\/[^\s<]+)|#(\d+)\b/g

export function linkify(html: string): string {
  if (!html) return html
  const parser = new DOMParser()
  const doc = parser.parseFromString(html.replaceAll('<br>', '\n').replaceAll('<div>', '\n').replaceAll('</div>', ''), 'text/html')
  doc.querySelectorAll('script, style').forEach(el => el.remove())

  const walk = (node: Node) => {
    if (node.nodeName === 'A') return

    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent || ''
      const replaced = text.replace(REF_REGEX, (_, url, id) =>
        url ? `<a href="${url}">${url}</a>` : `<a href="#${id}">#${id}</a>`)
      if (replaced !== text) {
        const fragment = doc.createDocumentFragment()
        const temp = doc.createElement('div')
        temp.innerHTML = replaced
        while (temp.firstChild) {
          fragment.appendChild(temp.firstChild)
        }
        node.parentNode?.replaceChild(fragment, node)
      }
    } else {
      for (let i = 0; i < node.childNodes.length; i++) {
        walk(node.childNodes[i])
      }
    }
  }

  walk(doc.body)
  return doc.body.innerHTML
}

export function handleDescriptionClick(e: MouseEvent | KeyboardEvent, onSearch?: (q: string) => void) {
  const a = (e.target as HTMLElement).closest('a')
  if (a) {
    if (e instanceof KeyboardEvent && e.key !== 'Enter') return
    e.preventDefault()
    const href = a.getAttribute('href') || ''
    if (/^#\d+$/.test(href)) onSearch?.(href)
    else window.location.assign(a.href)
  }
}
