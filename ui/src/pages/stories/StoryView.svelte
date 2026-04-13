<script lang="ts">
  import {slide} from 'svelte/transition'
  import {type Id, type ProjectMemberUser, type Story, StoryStatus, StoryType} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from './StoryPointsSelect.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {onMount, tick} from 'svelte'
  import StoryActions from './StoryActions.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import StoryTagsEditor from './StoryTagsEditor.svelte'
  import StoryComments from './StoryComments.svelte'
  import {copyToClipboard} from './clipboard'
  import SelectField from 'src/forms/SelectField.svelte'
  import api from 'src/api/api'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {onStatusChanged} from './status'
  import {handleDescriptionClick, linkify} from 'src/shared/linkify'
  import {draggable} from 'src/shared/draggable'

  export let project: ProjectContext
  export let story: Story
  export let stories: Story[]
  export let movable = true
  export let onDrag: (detail: {id: Id<Story>, beforeId: Id<Story>}) => void = () => {}
  export let onSearch: (tag: string) => void = () => {}
  export let onSaved: (story: Story) => void = () => {}
  export let onDelete: (story: Story) => void = () => {}
  export let onLocate: ((story: Story) => void) | undefined = undefined
  export let highlightId: number | undefined = undefined
  export let flashId: number | undefined = undefined

  let view: HTMLElement
  let open = !story.id
  let highlighted = false

  export async function highlight(scroll = false) {
    highlighted = true
    if (scroll) {
      await tick()
      view?.scrollIntoView({behavior: 'smooth', block: 'nearest'})
    }
    setTimeout(() => highlighted = false, 2000)
  }

  $: if (highlightId && highlightId === story.id) {
    highlight(true)
    highlightId = undefined
  }

  $: if (flashId && flashId === story.id) {
    highlight()
    flashId = undefined
  }

  function onDrop(id: number, beforeId: number) {
    onDrag({id, beforeId})
  }

  $: reallyMovable = movable && !open

  async function save(move?: boolean) {
    open = false
    story = await api.post(`projects/${story.projectId}/stories`, story)
    if (story.assignedTo && !project.members[story.assignedTo]) {
      api.post<ProjectMemberUser>(`projects/${story.projectId}/members/me`).then(m => project.members[m.user.id] = m)
    }
    onSaved(story)
    if (move) setTimeout(() => {
      const sOrd = statusOrd(story.status)
      const before = stories.find(s => statusOrd(s.status) > sOrd)
      if (!before || before.order > story.order) return
      onDrag({id: story.id, beforeId: before.id})
      setTimeout(() => scrollIntoView(), 100)
    })
  }

  function statusOrd(s: StoryStatus) {
    return Object.values(StoryStatus).indexOf(s)
  }

  function saveOnEnter(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault()
      save()
    }
  }

  function handleKeyDown(e: KeyboardEvent) {
    handleDescriptionClick(e)
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) save()
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
<div bind:this={view} class="{open ? 'bg-stone-200 shadow-inner' : story.type == StoryType.RELEASE ? 'bg-blue-300 hover:bg-blue-400' : story.acceptedAt ? 'bg-green-100 hover:bg-success-200' : 'bg-stone-50 hover:bg-yellow-100'}
  flex flex-col border-b"
  use:draggable={{id: story.id, type: 'story', onDrop}} draggable={reallyMovable}
  class:highlight={highlighted}
>
  <!--svelte-ignore a11y-click-events-have-key-events -->
  <div class="sm:flex justify-between items-center gap-x-2 gap-y-0.5 px-3 py-2" class:cursor-move={reallyMovable}
       on:click={() => open = !open} role="button" tabindex="0">
    <span title={t.stories.types[story.type]} class="max-sm:float-left mr-1">
      {#if story.type == StoryType.FEATURE}
        <Icon name="star-filled" class="text-yellow-500"/>
      {:else if story.type == StoryType.CHORE}
        <Icon name="settings-filled" class="text-neutral-500"/>
      {:else if story.type == StoryType.BUG}
        <Icon name="bug" class="text-red-600"/>
      {:else if story.type == StoryType.RELEASE}
        <Icon name="release" class="text-black"/>
      {/if}
    </span>

    <div class="flex-grow">
      {#if open}
        <!-- svelte-ignore a11y-autofocus -->
        <div class="title flex-1 focus:bg-white focus:p-1 focus:-my-1" contenteditable="plaintext-only" bind:innerText={story.name}
             on:click|stopPropagation on:keydown={saveOnEnter} autofocus={!story.id}></div>
      {:else}
        <span class="title flex-1">{story.name}</span>
        {#if story.assignedTo}
          {@const m = project.members?.[story.assignedTo]}
          <span title={m?.user.name}>({m?.user.initials})</span>
        {/if}
        <div class="w-full flex flex-wrap gap-x-2.5 text-sm font-bold">
          {#each story.tags as tag}
            <button class="whitespace-nowrap cursor-pointer {project.epicTags?.has(tag) ? 'text-purple-700 border border-purple-300 rounded px-1' : 'text-green-700 hover:text-green-600'}"
                on:click|stopPropagation={() => onSearch(tag)}>{tag}</button>
          {/each}
        </div>
      {/if}
    </div>

    <div class="flex items-center gap-3 max-sm:float-right">
      <StoryActions {story} {save} {open} onLocate={story.iteration ? undefined : onLocate} disabled={!project.canEdit}/>
      <StoryPointsSelect bind:points={story.points} onchange={() => save()} disabled={project.currentIterationNum > story.iteration!}/>
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
          <Button icon="trash" title={t.stories.delete} variant="ghost" size="sm" on:click={() => onDelete(story)}/>
        </div>
        <div title="{t.stories.updatedAt} {formatDateTime(story.updatedAt)}">
          {project.members[story.createdBy!]?.user.name} {formatDateTime(story.createdAt)}
        </div>
      </div>

      <div class="flex gap-2">
        <SelectField bind:value={story.type} options={t.stories.types} title={t.stories.type}/>
        <SelectField bind:value={story.status} options={t.stories.statuses} title={t.stories.status} on:change={() => onStatusChanged(story)}/>
        <SelectField bind:value={story.assignedTo} emptyOption="" options={Object.values(project.members).map(m => [m.user.id, m.user.name]).toObject()} title={t.stories.assignedTo}/>
      </div>

      <h4>{t.stories.description}</h4>
      <div class="bg-white whitespace-pre-line p-2 min-h-16" bind:innerHTML={story.description} contenteditable="true"
           on:blur={() => story.description = linkify(story.description || '')}
           on:click={handleDescriptionClick} on:keydown={handleKeyDown} role="textbox" tabindex="0"></div>

      <h4>{t.stories.tags}</h4>
      <StoryTagsEditor {project} bind:story/>

      <StoryComments {project} bind:comments={story.comments} urlBase="/api/projects/{story.projectId}/stories/{story.id}" onSave={() => save()}/>
    </div>
  {/if}
</div>

<style lang="postcss">
  .highlight {
    animation: flash 2s ease-out;
  }

  @keyframes flash {
    0% { background-color: #fef08a; }
  }

  h4 {
    text-transform: uppercase;
    font-size: 0.8rem;
    @apply py-1;
  }
</style>
