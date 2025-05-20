<script lang="ts">
  import {slide} from 'svelte/transition'
  import {type Id, type Story, type StoryAttachment, type StoryComment, StoryType} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from 'src/pages/stories/StoryPointsSelect.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {createEventDispatcher, onMount} from 'svelte'
  import StoryActions from 'src/pages/stories/StoryActions.svelte'
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import {user} from 'src/stores/auth'
  import Button from 'src/components/Button.svelte'
  import StoryTagsEditor from 'src/pages/stories/StoryTagsEditor.svelte'
  import StoryTypeSelect from 'src/pages/stories/StoryTypeSelect.svelte'
  import {copyToClipboard} from 'src/pages/stories/clipboard'

  export let story: Story
  export let movable = true

  let view: HTMLElement
  let open = !story.id

  const dispatch = createEventDispatcher<{drag: {id: Id<Story>, beforeId: Id<Story>}, search: string, saved: Story, delete: Story}>()

  let isDropTarget = false
  function onDrop(e: DragEvent) {
    dispatch('drag', {id: parseInt(e.dataTransfer?.getData('id')!), beforeId: story.id})
    isDropTarget = false
  }

  $: reallyMovable = movable && !open && !story.acceptedAt

  async function save() {
    story = await api.post(`projects/${story.projectId}/stories`, story)
    open = false
    dispatch('saved', story)
  }

  function saveOnEnter(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault()
      save()
    }
  }

  function addComment() {
    story.comments = [...story.comments, {
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      createdBy: $user.id,
      attachments: [] as StoryAttachment[],
    } as StoryComment]
  }

  onMount(() => {
    if (open) view.scrollIntoView({behavior: 'smooth', block: 'nearest'})
  })
</script>

<div bind:this={view} class="{open ? 'bg-stone-200 shadow-inner' : story.type == StoryType.RELEASE ? 'bg-blue-300 hover:bg-blue-400' : story.acceptedAt ? 'bg-green-100 hover:bg-success-200' : 'bg-stone-50 hover:bg-yellow-100'}
      flex flex-col border-b {reallyMovable ? 'cursor-move' : 'cursor-default'}" draggable={reallyMovable}
     on:dragstart={e => e.dataTransfer?.setData('id', story.id.toString())} on:drop={onDrop}
     on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:drop-target={isDropTarget}
>
  <div class="flex justify-between items-start gap-x-2 gap-y-0.5 px-3 py-2" on:click={() => open = !open}>
    {#if story.type == StoryType.FEATURE}
      <Icon name="star-filled" class="text-yellow-500"/>
    {:else if story.type == StoryType.CHORE}
      <Icon name="settings-filled" class="text-neutral-500"/>
    {:else if story.type == StoryType.BUG}
      <Icon name="bug" class="text-red-600"/>
    {:else if story.type == StoryType.RELEASE}
      <Icon name="release" class="text-black"/>
    {/if}

    <div class="flex-grow">
      {#if open}
        <div class="title flex-1 focus:bg-white focus:p-1 focus:-my-1" contenteditable="true" bind:innerText={story.name} on:click|stopPropagation on:keydown={saveOnEnter} autofocus={!story.id}></div>
      {:else}
        <span class="title flex-1">{story.name}</span>
      {/if}

      <ul class="w-full flex gap-2.5 text-sm text-green-700 font-bold">
        {#each story.tags as tag}
          <li class="hover:text-green-600 cursor-pointer" on:click|stopPropagation={() => dispatch('search', tag)}>{tag}</li>
        {/each}
      </ul>
    </div>
    <div class="flex items-center gap-3">
      <StoryActions {story} {save} {open}/>
      <StoryPointsSelect bind:points={story.points} onchange={save}/>
      <Icon name={open ? 'chevron-up' : 'chevron-down'} class="cursor-pointer"/>
    </div>
  </div>
  {#if open}
    <div class="bg-stone-200 p-2" transition:slide>
      <div class="flex justify-between items-center text-sm text-muted pb-2 -mt-2">
        <div class="flex items-center">
          {#if story.id}
            <button on:click|stopPropagation={copyToClipboard} title={t.general.copy}>#{story.id}</button>
          {/if}
          <Button icon="trash" title={t.stories.delete} variant="ghost" size="sm" on:click={() => dispatch('delete', story)}/>
        </div>
        <div title="{t.stories.updatedAt} {formatDateTime(story.updatedAt)}">
          {formatDateTime(story.createdAt)}
        </div>
      </div>

      <div class="grid grid-cols-2 p-2 bg-gray-100 border border-solid border-gray-300 text-gray-500 text-sm">
        <span>{t.stories.type}</span>
        <div>
          <StoryTypeSelect bind:type={story.type}
                           class="p-1 -m-1 float-right text-sm text-gray-600 hover:text-black"/>
        </div>
      </div>

      <h4>{t.stories.description}</h4>
      <div class="bg-white whitespace-pre-line p-2" bind:innerHTML={story.description} contenteditable="true"></div>

      <h4>{t.stories.tags}</h4>
      <StoryTagsEditor bind:story on:keydown={saveOnEnter}/>

      <h4>{t.stories.comments}</h4>
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
      <Button variant="outlined" size="sm" class="mt-2" on:click={addComment}>＋</Button>
    </div>
  {/if}
</div>

<style global>
  h4 {
    text-transform: uppercase;
    font-size: 0.8rem;
    @apply py-1;
  }

  .drop-target {
    box-shadow: inset 0 1px 0 0 black;
  }
</style>
