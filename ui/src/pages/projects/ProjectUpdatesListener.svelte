<script lang="ts">
  import {type Project, type Story, StoryStatus} from 'src/api/types'
  import {onMount} from 'svelte'
  import {requesterId} from 'src/api/api'

  export let project: Project
  export let stories: Story[]
  export let onStoryUpdated: (story: Story) => void = () => {}

  let updates: EventSource

  onMount(() => {
    listen()
    return () => updates?.close()
  })

  function listen() {
    updates?.close()
    updates = new EventSource(`/api/projects/${project.id}/updates/${requesterId}`)
    updates.addEventListener('story', e => {
      const story = JSON.parse(e.data) as Story
      let index = stories.findIndex(s => s.id == story.id)
      if (index >= 0) {
        if (story.status == StoryStatus.DELETED) {stories.splice(index, 1); return stories = stories}
        else if (stories[index].order == story.order) {
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
  }
</script>
