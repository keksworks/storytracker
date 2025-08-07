<script lang="ts">
  import {slide} from 'svelte/transition'
  import {type Id, type ProjectMemberUser, type Story, type StoryAttachment, type StoryComment, StoryStatus, StoryType} from 'src/api/types'
  import Icon from 'src/icons/Icon.svelte'
  import StoryPointsSelect from 'src/pages/stories/StoryPointsSelect.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {onMount} from 'svelte'
  import StoryActions from 'src/pages/stories/StoryActions.svelte'
  import {t} from 'src/i18n'
  import {user} from 'src/stores/auth'
  import Button from 'src/components/Button.svelte'
  import StoryTagsEditor from 'src/pages/stories/StoryTagsEditor.svelte'
  import {copyToClipboard} from 'src/pages/stories/clipboard'
  import SelectField from 'src/forms/SelectField.svelte'
  import api from 'src/api/api'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let project: ProjectContext
  export let story: Story
  export let movable = true
  export let firstUnstarted: Story | undefined
  export let firstUnaccepted: Story | undefined
  export let onDrag: (detail: {id: Id<Story>, beforeId?: Id<Story>}) => void = () => {}
  export let onSearch: (tag: string) => void = () => {}
  export let onSaved: (story: Story) => void = () => {}
  export let onDelete: (story: Story) => void = () => {}

  let view: HTMLElement
  let open = !story.id

  let isDropTarget = false
  function onDrop(e: DragEvent) {
    onDrag({id: parseInt(e.dataTransfer?.getData('id')!), beforeId: story.id})
    isDropTarget = false
  }

  $: reallyMovable = movable && !open

  async function save(move?: boolean) {
    open = false
    story = await api.post(`projects/${story.projectId}/stories`, story)
    if (story.assignedTo && !project.members.find(m => m.user.id === story.assignedTo)) {
      api.post<ProjectMemberUser>(`projects/${story.projectId}/members/me`).then(m => project.members = [...project.members, m])
    }
    onSaved(story)
    if (move) setTimeout(() => {
      const beforeId = story.status === StoryStatus.ACCEPTED ? firstUnaccepted?.id : firstUnstarted?.id
      onDrag({id: story.id, beforeId})
      setTimeout(() => scrollIntoView(), 100)
    }, 100)
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
      flex flex-col border-b {reallyMovable ? 'cursor-move' : 'cursor-default'}" draggable={reallyMovable}
     on:dragstart={e => e.dataTransfer?.setData('id', story.id.toString())} on:drop={onDrop}
     on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:drop-target={isDropTarget}
>
  <!--svelte-ignore a11y-click-events-have-key-events -->
  <div class="flex justify-between items-center gap-x-2 gap-y-0.5 px-3 py-2" on:click={() => open = !open}>
    <span title={t.stories.types[story.type]}>
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
        <div class="title flex-1 focus:bg-white focus:p-1 focus:-my-1" contenteditable="plaintext-only" bind:innerText={story.name}
             on:click|stopPropagation on:keydown={saveOnEnter} autofocus={!story.id}></div>
      {:else}
        <span class="title flex-1">{story.name}</span>
        {#if story.assignedTo}
          {@const m = project.members?.find(m => m.user.id === story.assignedTo)}
          <span title="{m?.user.firstName} {m?.user.lastName}">({m?.user.initials})</span>
        {/if}
        <ul class="w-full flex flex-wrap gap-x-2.5 text-sm text-green-700 font-bold">
          {#each story.tags as tag}
            <li class="hover:text-green-600 whitespace-nowrap cursor-pointer" on:click|stopPropagation={() => onSearch(tag)}>{tag}</li>
          {/each}
        </ul>
      {/if}
    </div>
    <div class="flex items-center gap-3">
      <StoryActions {story} {save} {open}/>
      <StoryPointsSelect bind:points={story.points} onchange={() => save()} disabled={!!story.acceptedAt}/>
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
          {formatDateTime(story.createdAt)}
        </div>
      </div>

      <div class="flex gap-2">
        <SelectField bind:value={story.type} options={t.stories.types} title={t.stories.type}/>
        <SelectField bind:value={story.status} options={t.stories.statuses} title={t.stories.status}/>
      </div>

      <h4>{t.stories.description}</h4>
      <div class="bg-white whitespace-pre-line p-2" bind:innerHTML={story.description} contenteditable="true"></div>

      <h4>{t.stories.tags}</h4>
      <StoryTagsEditor {project} bind:story/>

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
