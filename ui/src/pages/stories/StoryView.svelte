<script lang="ts">
  import {slide} from 'svelte/transition'
  import type {Id, Story} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from 'src/pages/stories/StoryPointsSelect.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {createEventDispatcher} from 'svelte'

  export let story: Story

  const dispatch = createEventDispatcher<{drag: {id: Id<Story>, beforeId: Id<Story>}}>()

  let isDropTarget = false
  function onDrop(e: DragEvent) {
    dispatch('drag', {id: e.dataTransfer?.getData('id')!, beforeId: story.id})
    isDropTarget = false
  }
</script>

<div class="bg-stone-50 hover:bg-yellow-100 flex flex-col border-b"
     class:cursor-move={!story.open} draggable={!story.open}
     on:dragstart={e => e.dataTransfer?.setData('id', story.id)} on:drop={onDrop}
     on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:border-t-black={isDropTarget}
>
  <div class="flex justify-between items-start gap-x-2 gap-y-0.5 px-3 py-2" on:click={() => story.open = !story.open}>
    {#if Math.random() > 0.2}
      <Icon name="star-filled" class="text-yellow-500"/>
    {:else if Math.random() > 0.4}
      <Icon name="settings-filled" class="text-neutral-500"/>
    {:else}
      <Icon name="bug" class="text-red-600"/>
    {/if}
    <div class="flex-grow">
      <span class="title flex-1">{story.title}</span>
      <ul class="w-full flex gap-1 text-sm text-green-700 font-bold">
        {#each story.tags as tag}
          <li class="hover:text-green-600">{tag}</li>
        {/each}
      </ul>
    </div>
    <StoryPointsSelect bind:points={story.points}/>
    <Icon name={story.open ? 'chevron-up' : 'chevron-down'}/>
  </div>
  {#if story.open}
    <div class="bg-stone-100 px-3 py-2 shadow-inner" transition:slide>
      <div class="flex justify-between text-sm text-muted pb-2">
        <div>#{story.id}</div>
        <div>{formatDateTime(story.createdAt)}</div>
      </div>
      <div class="bg-white whitespace-pre-line p-2 -mx-3 -mb-2" bind:innerHTML={story.description} contenteditable="true"></div>
    </div>
  {/if}
</div>
