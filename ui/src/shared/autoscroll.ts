const SCROLL_ZONE = 60
const SCROLL_SPEED = 8

let scrollRaf = 0

export function autoscroll(e: DragEvent) {
  cancelAnimationFrame(scrollRaf)
  const panel = (e.target as Element).closest('.panel')
  if (!panel) return
  const {top, bottom} = panel.getBoundingClientRect()
  if (e.clientY - top < SCROLL_ZONE) scroll(panel, -1)
  else if (bottom - e.clientY < SCROLL_ZONE) scroll(panel, 1)
}

export function stopAutoscroll() {
  cancelAnimationFrame(scrollRaf)
}

function scroll(panel: Element, dir: -1 | 1) {
  panel.scrollTop += dir * SCROLL_SPEED
  scrollRaf = requestAnimationFrame(() => scroll(panel, dir))
}
