<script lang="ts">
  import {slide} from 'svelte/transition'
  import {type Epic, type Story, StoryStatus} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {onMount} from 'svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import {copyToClipboard} from 'src/pages/stories/clipboard'
  import api from 'src/api/api'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {handleDescriptionClick, linkify} from 'src/shared/linkify'
  import StoryComments from 'src/pages/stories/StoryComments.svelte'

  export let project: ProjectContext
  export let epic: Epic
  export let stories: Story[]
  export let movable = false
  export let onSaved: (epic: Epic) => void = () => {}
  export let onDelete: (epic: Epic) => void = () => {}
  export let onSearch: (tag: string) => void = () => {}
  export let onStorySaved: (story: Story) => void = () => {}

  let view: HTMLElement
  let open = !epic.id

  $: reallyMovable = movable && !open
  $: taggedStories = stories.filter(s => s.tags?.includes(epic.tag))
  $: acceptedCount = taggedStories.filter(s => s.status === StoryStatus.ACCEPTED).length
  $: finishedCount = taggedStories.filter(s => [StoryStatus.FINISHED, StoryStatus.DELIVERED].includes(s.status)).length
  $: total = taggedStories.length || 1
  $: acceptedPercent = Math.round(acceptedCount / total * 100)
  $: finishedPercent = Math.round(finishedCount / total * 100)

  let isDropTarget = false

  async function onDrop(e: DragEvent) {
    isDropTarget = false
    const id = parseInt(e.dataTransfer?.getData('id')!)
    const story = stories.find(s => s.id === id)
    if (story && !story.tags?.includes(epic.tag)) {
      story.tags = [...(story.tags || []), epic.tag]
      const saved = await api.post<Story>(`projects/${story.projectId}/stories`, story)
      onStorySaved(saved)
    }
  }

  async function save() {
    epic.tag ||= epic.name.toLowerCase()
    epic = await api.post(`projects/${epic.projectId}/epics`, epic)
    project.epicTags.add(epic.tag)
    onSaved(epic)
  }

  function saveOnEnter(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault()
      save()
    }
  }

  function scrollIntoView() {
    view?.scrollIntoView({behavior: 'smooth', block: 'nearest'})
  }

  onMount(() => {
    if (open) {
      scrollIntoView();
      (view?.querySelector('[autofocus]') as HTMLInputElement)?.focus()
    }
  })
</script>

<!--svelte-ignore a11y-no-static-element-interactions -->
<div bind:this={view} class="{open ? 'bg-stone-200 shadow-inner' : 'bg-purple-100 hover:bg-purple-200'}
      flex flex-col border-b" draggable={reallyMovable}
     on:dragstart={e => e.dataTransfer?.setData('id', epic.id.toString())}
     on:drop={onDrop} on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:drop-target={isDropTarget}
>
  <!--svelte-ignore a11y-click-events-have-key-events -->
  <div class="sm:flex justify-between items-center gap-x-2 gap-y-0.5 px-3 py-2 cursor-pointer" class:cursor-move={reallyMovable}
       on:click={() => open = !open} role="button" tabindex="0">
    <span title={t.panels.epics} class="max-sm:float-left mr-1">
      <Icon name="epics" class="text-purple-600"/>
    </span>

    <div class="flex-grow">
      {#if open}
        <!-- svelte-ignore a11y-autofocus -->
        <div class="title flex-1 focus:bg-white focus:p-1 focus:-my-1" contenteditable="plaintext-only" bind:innerText={epic.name}
             on:click|stopPropagation on:keydown={saveOnEnter} autofocus={!epic.id}></div>
      {:else}
        <span class="title flex-1">{epic.name}</span>
        {#if epic.tag}
          <button class="text-sm text-purple-700 font-bold ml-2 border border-purple-300 rounded px-1 cursor-pointer"
                on:click|stopPropagation={() => onSearch(epic.tag)}>{epic.tag}</button>
        {/if}
      {/if}
    </div>

    <div class="flex items-center gap-3 max-sm:float-right">
      {#if open && project.canEdit}
        <Button size="sm" variant="solid" on:click={() => save()}>{t.general.save}</Button>
      {/if}
      <Icon name={open ? 'chevron-up' : 'chevron-down'} class="cursor-pointer"/>
    </div>
  </div>
  {#if !open && taggedStories.length}
    <div class="h-[2px] w-full bg-purple-100 flex">
      <div class="h-full bg-purple-600 transition-all duration-500" style="width: {acceptedPercent}%" title="{acceptedCount} {t.stories.statuses.ACCEPTED}"></div>
      <div class="h-full bg-purple-300 transition-all duration-500" style="width: {finishedPercent}%" title="{finishedCount} {t.stories.statuses.FINISHED}"></div>
    </div>
  {/if}
  {#if open}
    <div class="bg-stone-200 p-2" transition:slide>
      <div class="flex justify-between items-center text-sm text-muted pb-2 -mt-2">
        <div class="flex items-center">
          {#if epic.id}
            <button on:click|stopPropagation={copyToClipboard} title={t.general.copy}>#{epic.id}</button>
          {/if}
          {#if project.canEdit}
            <Button icon="trash" title={t.epics.delete} variant="ghost" size="sm" on:click={() => onDelete(epic)}/>
          {/if}
        </div>
        {#if epic.createdAt}
          <div title="{t.stories.updatedAt} {epic.updatedAt ? formatDateTime(epic.updatedAt) : ''}">
            {formatDateTime(epic.createdAt)}
          </div>
        {/if}
      </div>

      <div class="flex gap-2 items-center mb-2">
         <span class="text-sm font-semibold uppercase">{t.epics.tag}:</span>
         <div class="bg-white px-2 py-1 rounded text-purple-700 font-bold flex-1" contenteditable="plaintext-only" bind:innerText={epic.tag}></div>
      </div>

      <h4>{t.stories.description}</h4>
      <div class="bg-white whitespace-pre-line p-2 min-h-16" bind:innerHTML={epic.description} contenteditable="true"
           on:blur={() => epic.description = linkify(epic.description || '')}
           on:click={handleDescriptionClick} on:keydown={handleDescriptionClick} role="textbox" tabindex="0"></div>

      <StoryComments {project} bind:comments={epic.comments} urlBase={`/api/projects/${epic.projectId}/epics/${epic.id}`}/>
    </div>
  {/if}
</div>

<style lang="postcss">
  .drop-target {
    @apply bg-purple-200;
  }
</style>
