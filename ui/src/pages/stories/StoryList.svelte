<script lang="ts">
  import type {Story} from 'src/api/types'
  import {pointsLabel} from 'src/pages/stories/points'
  import Icon from 'src/icons/Icon.svelte'

  export let stories: Story[]
</script>

{#each stories as s}
  <div class="story flex gap-1">
    <span class="title flex-1">{s.title}</span>
    <span class="points">{pointsLabel(s.points)}</span>
    <Icon name="chevron-down"/>
  </div>
{/each}

<style>
  .story {
    @apply p-2 border bg-yellow-50 hover:bg-yellow-100
  }

  .points {
    @apply text-blue-500 font-bold
  }
</style>
