<script lang="ts">
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import {type Id, type Project, type Story, type StoryBlocker, type StoryComment, type StoryMoveRequest, StoryStatus, StoryType} from 'src/api/types'
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'
  import Icon from 'src/icons/Icon.svelte'
  import Button from 'src/components/Button.svelte'
  import Header from 'src/layout/Header.svelte'
  import {onMount} from 'svelte'
  import FormField from 'src/forms/FormField.svelte'

  export let id: Id<Project>

  let project: Project | undefined
  let stories: Story[] = []
  let searchQuery: string | undefined
  let searchResults: Story[] | undefined
  let velocity = 10

  function changeVelocity() {
    const v = parseInt(prompt(t.projects.velocityOverride, velocity.toString())!)
    if (v) velocity = v
  }

  async function loadStories(fromIteration: number) {
    stories = await api.get<Story[]>(`projects/${id}/stories?fromIteration=${fromIteration}`)
  }

  async function search(q?: string) {
    searchQuery = q
    searchResults = undefined
    if (q) {
      searchResults = await api.get<Story[]>(`projects/${id}/stories?q=${encodeURIComponent(q)}`)
    } else {
      searchQuery = undefined
    }
  }

  let show: Record<string, boolean> = {
    done: false,
    backlog: true,
    icebox: true,
  }

  let pastLoaded = false

  onMount(async () => {
    project = await api.get('projects/' + id)
    await loadStories(project!.currentIterationNum)
  })

  $: if (show.done && !pastLoaded) {
    loadStories(0)
    pastLoaded = true
  }

  $: done = stories.filter(s => s.iteration! < project!.currentIterationNum)
  $: icebox = stories.filter(s => s.status === StoryStatus.UNSCHEDULED)
  $: backlog = stories.filter(s => s.status !== StoryStatus.UNSCHEDULED && (!s.iteration || s.iteration >= project!.currentIterationNum))

  async function onDrag(e: CustomEvent<{id: Id<Story>, beforeId: Id<Story>}>) {
    const fromIndex = stories.findIndex(s => s.id == e.detail.id)
    const story = stories.splice(fromIndex, 1)[0]
    const toIndex = stories.findIndex(s => s.id == e.detail.beforeId)
    stories.splice(toIndex, 0, story)
    story.order = newOrder(stories[toIndex - 1], stories[toIndex])
    stories = stories
    stories[toIndex] = await api.post<Story>(`projects/${id}/stories`, story)
  }

  function addStory(panel: Story[], status: StoryStatus) {
    const prev = panel.last()
    const index = stories.findIndex(s => s.id === prev.id) + 1
    const next = stories[index]
    const newStory = {
      status, projectId: project!.id, afterId: prev?.id, order: newOrder(prev, next),
      type: StoryType.FEATURE, tags: [] as string[], blockers: [] as StoryBlocker[], comments: [] as StoryComment[]
    } as Story
    stories.splice(index, 0, newStory)
    stories = stories
  }

  function newOrder(prev?: Story, next?: Story) {
    return (prev?.order ?? 0) + (next ? (next.order - prev.order) / 2 : 1)
  }
</script>

<svelte:head>
  {#if project?.name}
    <title>{project?.name} - {t.title}</title>
  {/if}
</svelte:head>

<div class="h-screen overflow-hidden flex flex-col">
  <Header title={project?.name}>
    <FormField type="search" placeholder={t.stories.search.placeholder} on:keydown={e => e.key == 'Enter' && search(e.currentTarget?.['value'])}/>
  </Header>
  <div class="flex h-screen px-4">
    <aside class="w-14 sticky top-0 h-screen pt-3 -ml-3">
      <div class="flex flex-col items-center gap-4">
        {#each Object.keys(show) as key}
          <Button icon={key} size="lg" title={t.panels[key]} on:click={() => show[key] = !show[key]}
                  variant={show[key] ? 'solid' : 'ghost'} color="secondary"/>
        {/each}
      </div>
    </aside>
    {#if !project || !stories}
      <Spinner/>
    {:else}
      <div class="flex gap-2 ml-1 mt-3 w-full">
        {#if show.done}
          <div class="panel">
            <h5 class="panel-title">
              <span><Icon name="done" size="lg"/> {t.panels.done}</span>
              <Button title={t.general.close} on:click={() => show.done = false} variant="ghost">✕</Button>
            </h5>
            <StoryList stories={done} on:search={e => search(e.detail)} movable={false}/>
          </div>
        {/if}
        {#if show.backlog}
          <div class="panel">
            <h5 class="panel-title">
              <span>
                <Icon name="backlog" size="lg"/> {t.panels.backlog}
                <button title={t.projects.velocity} class="px-2 hover:bg-stone-200" on:click={changeVelocity}>⚡{velocity}</button>
              </span>
              <span>
                <Button label="＋ {t.stories.add}" on:click={() => addStory(backlog, StoryStatus.UNSTARTED)} variant="outlined"/>
                <Button title={t.general.close} on:click={() => show.backlog = false} variant="ghost">✕</Button>
              </span>
            </h5>
            <StoryList stories={backlog} {velocity} on:search={e => search(e.detail)} on:drag={onDrag}/>
          </div>
        {/if}
        {#if show.icebox}
          <div class="panel">
            <h5 class="panel-title">
              <span><Icon name="icebox" size="lg"/> {t.panels.icebox}</span>
              <span>
                <Button label="＋ {t.stories.add}" on:click={() => addStory(icebox, StoryStatus.UNSCHEDULED)} variant="outlined"/>
                <Button title={t.general.close} on:click={() => show.icebox = false} variant="ghost">✕</Button>
              </span>
            </h5>
            <StoryList stories={icebox} on:search={e => search(e.detail)} on:drag={onDrag}/>
          </div>
        {/if}
        {#if searchQuery}
          <div class="panel">
            <h5 class="panel-title">
              <span><Icon name="search" size="lg"/> {t.stories.search.title}</span>
              {searchQuery}
              <Button title={t.general.close} on:click={() => searchQuery = undefined} variant="ghost">✕</Button>
            </h5>
            {#if searchResults}
              <StoryList stories={searchResults} movable={false} on:search={e => search(e.detail)}/>
            {:else}
              <Spinner/>
            {/if}
          </div>
        {/if}
      </div>
    {/if}
  </div>
</div>

<style lang="postcss">
  .panel {
    @apply w-full max-w-6xl overflow-y-auto flex flex-col bg-gray-100 border border-gray-200 rounded;
  }

  .panel-title {
    @apply py-2 px-3 text-lg font-semibold sticky top-0 bg-gray-100 border-b flex justify-between items-center;
  }
</style>
