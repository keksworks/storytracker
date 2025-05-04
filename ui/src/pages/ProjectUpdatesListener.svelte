<script lang="ts">
  import {type Project, type Story, StoryStatus} from 'src/api/types'
  import {onMount} from 'svelte'

  export let project: Project
  export let stories: Story[]

  let updates: EventSource

  onMount(() => {
    listenToUpdates()
    return () => updates?.close()
  })

  function listenToUpdates() {
    updates?.close()
    const lastUpdatedAt = stories.max(s => s.updatedAt)
    updates = new EventSource(`/api/projects/${project.id}/updates` + (lastUpdatedAt ? '?after=' + lastUpdatedAt : ''))
    updates.addEventListener('story', e => {
      const story = JSON.parse(e.data) as Story
      let index = stories.findIndex(s => s.id == story.id)
      if (index >= 0) {
        if (story.status == StoryStatus.DELETED) return stories.splice(index, 1)
        else if (stories[index].order == story.order) return stories[index] = story
        else stories.splice(index, 1)
      }
      index = stories.findIndex(s => s.order > story.order) - 1
      if (index < 0) index = stories.length
      stories.splice(index, 0, story)
      stories = stories
    })
  }

  let becameHidden = Date.now()
  function visibilityChange() {
    if (document.hidden) updates?.close()
    else {
      if (Date.now() - becameHidden > 30 * 60 * 1000) location.reload()
      else listenToUpdates()
    }
  }
</script>

<svelte:window on:visibilitychange={visibilityChange}/>
