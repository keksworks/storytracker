<script lang="ts">
  import type {Story} from 'src/api/types'
  import StoryView from 'src/pages/stories/StoryView.svelte'

  export let stories: Story[]
</script>

{#each stories as story (story.id)}
  <StoryView {story}/>
{/each}
