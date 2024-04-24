<script lang="ts">
  import type {Id, Story} from 'src/api/types'
  import StoryView from 'src/pages/stories/StoryView.svelte'

  export let stories: Story[]

  function onDrag(e: CustomEvent<{id: Id<Story>, beforeId: Id<Story>}>) {
    const fromIndex = stories.findIndex(s => s.id == e.detail.id)
    const story = stories.splice(fromIndex, 1)
    const toIndex = stories.findIndex(s => s.id == e.detail.beforeId)
    stories.splice(toIndex, 0, ...story)
    stories = stories
  }
</script>

{#each stories as story (story.id)}
  <StoryView {story} on:drag={onDrag}/>
{/each}
