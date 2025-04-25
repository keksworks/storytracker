<script lang="ts">
  import {slide} from 'svelte/transition'
  import {type Id, type Story, StoryType} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from 'src/pages/stories/StoryPointsSelect.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {createEventDispatcher} from 'svelte'
  import StoryStatus from 'src/pages/stories/StoryStatus.svelte'
  import {t} from 'src/i18n'

  export let story: Story & {open?: boolean}
  export let movable = true

  const dispatch = createEventDispatcher<{drag: {id: Id<Story>, beforeId: Id<Story>}, search: string}>()

  let isDropTarget = false
  function onDrop(e: DragEvent) {
    dispatch('drag', {id: parseInt(e.dataTransfer?.getData('id')!), beforeId: story.id})
    isDropTarget = false
  }

  $: reallyMovable = movable && !story.open && !story.acceptedAt

  async function copyToClipboard(e: Event) {
    const el = e.currentTarget as HTMLElement
    const v = el.textContent!
    await navigator.clipboard.writeText(v)
    el.textContent = t.general.copied
    setTimeout(() => el.textContent = v, 1000)
  }
</script>

<div class="{story.open ? 'bg-stone-200 shadow-inner' : story.type == StoryType.RELEASE ? 'bg-blue-300 hover:bg-blue-400' : story.acceptedAt ? 'bg-green-100 hover:bg-success-200' : 'bg-stone-50 hover:bg-yellow-100'}
      flex flex-col border-b {reallyMovable ? 'cursor-move' : 'cursor-default'}" draggable={reallyMovable}
     on:dragstart={e => e.dataTransfer?.setData('id', story.id.toString())} on:drop={onDrop}
     on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:drop-target={isDropTarget}
>
  <div class="flex justify-between items-start gap-x-2 gap-y-0.5 px-3 py-2" on:click={() => story.open = !story.open}>
    {#if story.type == StoryType.FEATURE}
      <Icon name="star-filled" class="text-yellow-500"/>
    {:else if story.type == StoryType.CHORE}
      <Icon name="settings-filled" class="text-neutral-500"/>
    {:else if story.type == StoryType.BUG}
      <Icon name="bug" class="text-red-600"/>
    {/if}
    <div class="flex-grow">
      <span class="title flex-1">{story.name}</span>
      <ul class="w-full flex gap-3 text-sm text-green-700 font-bold">
        {#each story.tags as tag}
          <li class="hover:text-green-600 cursor-pointer" on:click|stopPropagation={() => dispatch('search', tag)}>{tag}</li>
        {/each}
      </ul>
    </div>
    <div class="flex items-center gap-3">
      <StoryStatus bind:story/>
      <StoryPointsSelect bind:points={story.points}/>
      <Icon name={story.open ? 'chevron-up' : 'chevron-down'}/>
    </div>
  </div>
  {#if story.open}
    <div class="bg-stone-200 p-2" transition:slide>
      <div class="flex justify-between text-sm text-muted pb-2">
        <button on:click|stopPropagation={copyToClipboard} title={t.general.copy}>#{story.id}</button>
        <div title="{t.stories.updatedAt} {formatDateTime(story.updatedAt)}">
          {formatDateTime(story.createdAt)}
        </div>
      </div>
      <div class="bg-white whitespace-pre-line p-2" bind:innerHTML={story.description} contenteditable="true"></div>

      {#each story.comments as comment}
        <div>
          <div class="text-sm text-muted text-right py-2">{formatDateTime(comment.updatedAt)}</div>
          <div class="bg-white whitespace-pre-line p-2" bind:innerHTML={comment.text} contenteditable="true"></div>
          {#each comment.attachments as attachment}
            {@const url = `/api/projects/${story.projectId}/stories/${story.id}/attachments/${encodeURIComponent(attachment.filename)}`}
            <a href={url} target="_blank">
              {#if attachment.width && attachment.height}
                <img src={url} class="max-h-32 mt-2">
              {:else}
                {attachment.filename}
              {/if}
            </a>
          {/each}
        </div>
      {/each}
    </div>
  {/if}
</div>

<style>
  .drop-target {
    box-shadow: inset 0 1px 0 0 black;
  }
</style>
