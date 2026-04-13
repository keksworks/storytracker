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

  function applyListUpdate<T extends {id: number, updatedAt?: string, deleted?: boolean}>(list: T[], item: T): boolean {
    const index = list.findIndex(i => i.id === item.id)
    if (index >= 0) {
      if (list[index].updatedAt === item.updatedAt) return false
      if (item.deleted) list.splice(index, 1)
      else list[index] = item
    } else if (!item.deleted) {
      list.push(item)
    }
    return true
  }

  function listen() {
    updates?.close()
    updates = new EventSource(`/api/projects/${project.id}/updates/${requesterId}`)
    updates.addEventListener('story', e => {
      const story = JSON.parse(e.data) as Story
      let index = stories.findIndex(s => s.id == story.id)
      if (index >= 0) {
        if (stories[index].updatedAt === story.updatedAt) return
        else if (story.status == StoryStatus.DELETED) {
          stories.splice(index, 1)
          return stories = stories
        } else if (stories[index].order === story.order) {
          stories[index] = story
          onStoryUpdated(story)
          return
        }
        else stories.splice(index, 1)
      }
      index = stories.findIndex(s => s.order > story.order)
      if (index < 0) index = stories.length
      stories.splice(index, 0, story)
      onStoryUpdated(story)
      stories = stories
    })
    updates.addEventListener('epic', e => {
      const epic = JSON.parse(e.data) as Epic
      if (applyListUpdate(epics, epic)) epics = epics
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
