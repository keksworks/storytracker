<script lang="ts">
  import type {Story} from 'src/api/types'
  import {pointsLabel} from 'src/pages/stories/points'
  import Icon from 'src/icons/Icon.svelte'

  export let story: Story
</script>

<div class="story flex gap-1" on:click={() => story.open = !story.open}>
  <span class="title flex-1">{story.title}</span>
  <span class="points">{pointsLabel(story.points)}</span>
  <Icon name={story.open ? 'chevron-up' : 'chevron-down'}/>
</div>

<style>
  .story {
    @apply p-2 border bg-yellow-50 hover:bg-yellow-100
  }

  .points {
    @apply text-blue-500 font-bold text-sm
  }
</style>
