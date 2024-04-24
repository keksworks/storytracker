<script lang="ts">
  import {slide} from 'svelte/transition'
  import type {Story} from 'src/api/types'
  import {pointsLabel} from 'src/pages/stories/points'
  import Icon from 'src/icons/Icon.svelte'

  export let story: Story
</script>

<div class="flex flex-col">
  <div class="story flex gap-1" on:click={() => story.open = !story.open}>
    <span class="title flex-1">{story.title}</span>
    <span class="points">{pointsLabel(story.points)}</span>
    <Icon name={story.open ? 'chevron-up' : 'chevron-down'}/>
  </div>
  {#if story.open}
    <div class="shadow-inner" transition:slide>
      <div class="p-2 whitespace-pre-line" bind:innerHTML={story.description} contenteditable="true"></div>
    </div>
  {/if}
</div>

<style>
  .story {
    @apply p-2 border bg-yellow-50 hover:bg-yellow-100
  }

  .points {
    @apply text-blue-500 font-bold text-sm
  }
</style>
