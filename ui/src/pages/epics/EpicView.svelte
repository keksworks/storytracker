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
  export let movable = true
  export let onSaved: (epic: Epic) => void = () => {}
  export let onDelete: (epic: Epic) => void = () => {}
  export let onSearch: (tag: string) => void = () => {}

  let view: HTMLElement
  let open = !epic.id

  $: reallyMovable = movable && !open
  $: taggedStories = stories.filter(s => s.tags?.includes(epic.tag))
  $: acceptedCount = taggedStories.filter(s => s.status === StoryStatus.ACCEPTED).length
  $: progress = Math.round(acceptedCount / (taggedStories.length || 1) * 100)

  async function save() {
    epic.tag ||= epic.name.toLowerCase()
    epic = await api.post(`projects/${epic.projectId}/epics`, epic)
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
      flex flex-col border-b {reallyMovable ? 'cursor-move' : 'cursor-default'}" draggable={reallyMovable}
     on:dragstart={e => e.dataTransfer?.setData('id', epic.id.toString())}
>
  <!--svelte-ignore a11y-click-events-have-key-events -->
  <div class="sm:flex justify-between items-center gap-x-2 gap-y-0.5 px-3 py-2" on:click={() => open = !open}>
    <span title={t.panels.epics} class="max-sm:float-left mr-1">
      <Icon name="epics" class="text-purple-600"/>
    </span>

    <div class="flex-grow">
      {#if open}
        <div class="title flex-1 focus:bg-white focus:p-1 focus:-my-1" contenteditable="plaintext-only" bind:innerText={epic.name}
             on:click|stopPropagation on:keydown={saveOnEnter} autofocus={!epic.id}></div>
      {:else}
        <span class="title flex-1">{epic.name}</span>
        {#if epic.tag}
          <span class="text-sm text-purple-700 font-bold ml-2 border border-purple-300 rounded px-1 cursor-pointer"
                on:click|stopPropagation={() => onSearch(epic.tag)}>{epic.tag}</span>
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
  {#if epic.tag && taggedStories.length > 0}
    <div class="h-[2px] w-full bg-purple-200">
      <div class="h-full bg-purple-600 transition-all duration-500" style="width: {progress}%"></div>
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
           on:click={handleDescriptionClick}></div>

      <StoryComments {project} bind:comments={epic.comments} urlBase={`/api/projects/${epic.projectId}/epics/${epic.id}`}/>
    </div>
  {/if}
</div>

<style global>
  h4 {
    text-transform: uppercase;
    font-size: 0.8rem;
    @apply py-1;
  }
</style>
