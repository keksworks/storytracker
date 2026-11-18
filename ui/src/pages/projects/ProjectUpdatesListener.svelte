<script lang="ts">
  import {type Epic, type Project, type Story, StoryStatus} from 'src/api/types'
  import {onMount} from 'svelte'
  import {requesterId} from 'src/api/api'

  export let project: Project
  export let stories: Story[]
  export let epics: Epic[]
  export let onStoryUpdated: (story: Story) => void = () => {}

  let updates: EventSource

  onMount(() => {
    listen()
    return () => updates?.close()
  })

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

  function onEvent<T>(eventName: string, apply: (item: T) => void) {
    updates.addEventListener(eventName, e => apply(JSON.parse(e.data)))
  }

  function listen() {
    updates?.close()
    updates = new EventSource(`/api/projects/${project.id}/updates/${requesterId}`)
    onEvent<Story>('story', story => {
      if (applyListUpdate(stories, story, s => s.status == StoryStatus.DELETED, onStoryUpdated)) stories = stories
    })
    onEvent<Epic>('epic', epic => {
      if (applyListUpdate(epics, epic, ep => !!ep.deleted)) epics = epics
    })
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
