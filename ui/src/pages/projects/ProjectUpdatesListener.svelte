<script lang="ts">
  import {type Epic, type Project, type Story, StoryStatus} from 'src/api/types'
  import {onMount} from 'svelte'
  import {requesterId} from 'src/api/api'

  export let project: Project
  export let stories: Story[]
  export let epics: Epic[]
  export let onStoryUpdated: (story: Story) => void = () => {}

  const reconnectDelay = 2_000

  let updates: EventSource
  let lastEventId = ''
  let reconnectTimer: ReturnType<typeof setTimeout> | undefined

  onMount(() => {
    listen()
    return disconnect
  })

  function disconnect() {
    clearTimeout(reconnectTimer)
    reconnectTimer = undefined
    updates?.close()
  }

  function applyListUpdate<T extends {id: number, updatedAt?: string, order: number}>(
    list: T[], item: T, isDeleted: (i: T) => boolean, onUpdated?: (i: T) => void
  ): boolean {
    let index = list.findIndex(i => i.id === item.id)
    if (index >= 0) {
      if (list[index].updatedAt === item.updatedAt && list[index].order === item.order) return false
      if (isDeleted(item)) { list.splice(index, 1); return true }
      if (list[index].order === item.order) { list[index] = item; onUpdated?.(item); return true }
      list.splice(index, 1)
    } else if (isDeleted(item)) {
      return false
    }
    index = list.findIndex(i => i.order > item.order)
    if (index < 0) index = list.length
    list.splice(index, 0, item)
    onUpdated?.(item)
    return true
  }

  function onEvent<T>(source: EventSource, eventName: string, apply: (item: T) => void) {
    source.addEventListener(eventName, e => {
      lastEventId = e.lastEventId || lastEventId
      apply(JSON.parse(e.data))
    })
  }

  function listen() {
    disconnect()
    const after = lastEventId ? `?lastEventId=${encodeURIComponent(lastEventId)}` : ''
    updates = new EventSource(`/api/projects/${project.id}/updates/${requesterId}${after}`)
    const source = updates
    onEvent<Story>(source, 'story', story => {
      if (applyListUpdate(stories, story, s => s.status == StoryStatus.DELETED, onStoryUpdated)) stories = stories
    })
    onEvent<Epic>(source, 'epic', epic => {
      if (applyListUpdate(epics, epic, ep => !!ep.deleted)) epics = epics
    })
    source.onerror = () => {
      if (source.readyState === EventSource.CLOSED && !reconnectTimer) reconnectTimer = setTimeout(listen, reconnectDelay)
    }
  }

  let lastHiddenAt = 0
  function onVisibilityChange() {
    if (document.visibilityState === 'hidden')
      lastHiddenAt = Date.now()
    else if (lastHiddenAt && Date.now() - lastHiddenAt >= 6 * 60 * 60 * 1000)
      location.reload()
  }
</script>

<svelte:document on:visibilitychange={onVisibilityChange}/>
