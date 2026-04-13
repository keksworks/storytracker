import {autoscroll, stopAutoscroll} from 'src/shared/autoscroll'
import type {Id} from 'src/api/types'

type DragOptions = {
  id: number
  type: 'story' | 'epic'
  onDrop: (id: number, beforeId: number) => void
}

export function draggable(node: HTMLElement, {id, type, onDrop}: DragOptions) {
  node.ondragstart = (e: DragEvent) => {
    e.dataTransfer?.setData('text/plain', `${type}:${id}`)
  }

  node.ondragover = (e: DragEvent) => {
    e.preventDefault()
    node.classList.add('drop-line')
    autoscroll(e)
  }

  node.ondragleave = () => {
    node.classList.remove('drop-line')
    stopAutoscroll()
  }

  node.ondrop = (e: DragEvent) => {
    node.ondragleave!(e)
    const draggedId = getDraggedId(e, type)
    if (draggedId) onDrop(draggedId, id)
  }
}

export function getDraggedId(e: DragEvent, type: DragOptions['type']) {
  const data = e.dataTransfer?.getData('text/plain')
  if (!data?.startsWith(type)) return
  return parseInt(data.split(':')[1]!) as Id<any>
}
