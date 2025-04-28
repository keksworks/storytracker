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

  export let story: Story
  export let movable = true

  let view: HTMLElement
  let open = !story.id
  let tags = story.tags.join(', ')

  const dispatch = createEventDispatcher<{drag: {id: Id<Story>, beforeId: Id<Story>}, search: string, saved: Story}>()

  let isDropTarget = false
  function onDrop(e: DragEvent) {
    dispatch('drag', {id: parseInt(e.dataTransfer?.getData('id')!), beforeId: story.id})
    isDropTarget = false
  }

  $: reallyMovable = movable && !open && !story.acceptedAt

  async function copyToClipboard(e: Event) {
    const el = e.currentTarget as HTMLElement
    const v = el.textContent!
    await navigator.clipboard.writeText(v)
    el.textContent = t.general.copied
    setTimeout(() => el.textContent = v, 1000)
  }

  async function save() {
    story.tags = tags.split(',').map(t => t.trim()).filter(t => t)
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
    if (open) {
      const panel = view.closest('.panel') as HTMLElement
      if (panel && !isElementVisible(view, panel)) {
        view.scrollIntoView({behavior: 'smooth', block: 'nearest'})
      }
    }
  })

  function isElementVisible(element: HTMLElement, container: HTMLElement) {
    const {top, bottom} = element.getBoundingClientRect()
    const containerRect = container.getBoundingClientRect()
    return top >= containerRect.top && bottom <= containerRect.bottom
  }
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
      <StoryPointsSelect bind:points={story.points}/>
      <Icon name={open ? 'chevron-up' : 'chevron-down'}/>
    </div>
  </div>
  {#if open}
    <div class="bg-stone-200 p-2" transition:slide>
      <div class="flex justify-between text-sm text-muted pb-2">
        {#if story.id}
          <button on:click|stopPropagation={copyToClipboard} title={t.general.copy}>#{story.id}</button>
        {/if}
        <div title="{t.stories.updatedAt} {formatDateTime(story.updatedAt)}">
          {formatDateTime(story.createdAt)}
        </div>
      </div>
      <div class="bg-white whitespace-pre-line p-2" bind:innerHTML={story.description} contenteditable="true"></div>

      <h4>{t.stories.tags}</h4>
      <div class="bg-white p-2" bind:innerText={tags} contenteditable="true"></div>

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
