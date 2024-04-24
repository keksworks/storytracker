<script lang="ts">
  import {slide} from 'svelte/transition'
  import type {Story} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from 'src/pages/stories/StoryPointsSelect.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'

  export let story: Story
</script>

<div class="bg-yellow-50 hover:bg-yellow-100 flex flex-col border">
  <div class="flex gap-1 p-2 pb-0" on:click={() => story.open = !story.open}>
    <span class="title flex-1">{story.title}</span>
    <StoryPointsSelect bind:points={story.points}/>
    <Icon name={story.open ? 'chevron-up' : 'chevron-down'}/>
  </div>
  <ul class="flex gap-1 text-sm p-2 pt-0 text-green-700 font-bold">
    {#each story.tags as tag}
      <li class="hover:text-green-600">{tag}</li>
    {/each}
  </ul>
  {#if story.open}
    <div class="bg-white p-2 shadow-inner" transition:slide>
      <div class="flex justify-between text-sm text-muted pb-2">
        <div>#{story.id}</div>
        <div>{formatDateTime(story.createdAt)}</div>
      </div>
      <div class="whitespace-pre-line p-2 -m-2" bind:innerHTML={story.description} contenteditable="true"></div>
    </div>
  {/if}
</div>
