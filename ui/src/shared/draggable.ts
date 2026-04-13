import {autoscroll, stopAutoscroll} from 'src/shared/autoscroll'

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
    const data = e.dataTransfer?.getData('text/plain')
    if (!data?.startsWith(`${type}:`)) return
    const droppedId = parseInt(data.split(':')[1]!)
    onDrop(droppedId, id)
  }
}
