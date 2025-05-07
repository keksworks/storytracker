<script lang="ts">
  import {type Project, type Story, StoryStatus} from 'src/api/types'
  import {onMount} from 'svelte'

  export let project: Project
  export let stories: Story[]

  let updates: EventSource

  onMount(() => {
    listen()
    return () => updates?.close()
  })

  function listen() {
    updates?.close()
    updates = new EventSource(`/api/projects/${project.id}/updates`)
    updates.addEventListener('story', e => {
      const story = JSON.parse(e.data) as Story
      let index = stories.findIndex(s => s.id == story.id)
      if (index >= 0) {
        if (story.status == StoryStatus.DELETED) return stories.splice(index, 1)
        else if (stories[index].order == story.order) return stories[index] = story
        else stories.splice(index, 1)
      }
      index = stories.findIndex(s => s.order > story.order)
      if (index < 0) index = stories.length
      stories.splice(index, 0, story)
      stories = stories
    })
  }
</script>
