const URL_REGEX = /(https?:\/\/[^\s<]+)/g

export function linkify(html: string): string {
  if (!html) return html
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  doc.querySelectorAll('script, style').forEach(el => el.remove())

  const walk = (node: Node) => {
    if (node.nodeName === 'A') return

    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent || ''
      if (URL_REGEX.test(text)) {
        const fragment = doc.createDocumentFragment()
        const temp = doc.createElement('div')
        temp.innerHTML = text.replace(URL_REGEX, '<a href="$1">$1</a>')
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
