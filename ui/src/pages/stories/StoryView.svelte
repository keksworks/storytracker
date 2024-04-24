<script lang="ts">
  import {slide} from 'svelte/transition'
  import type {Story} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from 'src/pages/stories/StoryPointsSelect.svelte'

  export let story: Story
</script>

<div class="flex flex-col">
  <div class="story flex gap-1" on:click={() => story.open = !story.open}>
    <span class="title flex-1">{story.title}</span>
    <StoryPointsSelect bind:points={story.points}/>
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
</style>
