import {autoscroll, stopAutoscroll} from 'src/shared/autoscroll'
import type {Id} from 'src/api/types'

export type Dragged = {
  id?: Id<any>
  type?: 'story' | 'epic'
}

export type DragOptions = {
  id: Id<any>
  type: Dragged['type']
  onDrop: (id: Id<any>, beforeId: Id<any>, type: Dragged['type']) => void
}

export const dragged: Dragged = {}

export function draggable(node: HTMLElement, {id, type, onDrop}: DragOptions) {
  node.ondragstart = () => {
    dragged.id = id
    dragged.type = type
  }

  node.ondragover = (e: DragEvent) => {
    e.preventDefault()
    node.classList.add(dragClass())
    autoscroll(e)
  }

  node.ondragleave = () => {
    node.classList.remove(dragClass())
    stopAutoscroll()
  }

  node.ondrop = (e: DragEvent) => {
    node.ondragleave!(e)
    if (dragged.id) onDrop(dragged.id, id, dragged.type)
  }

  function dragClass() {
    return type === dragged.type ? 'drop-order' : 'drop-accept'
  }
}
