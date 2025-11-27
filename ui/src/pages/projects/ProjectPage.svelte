<script lang="ts">
  import {type Id, type Project, type ProjectMemberUser, type Story, type StoryBlocker, type StoryComment, StoryStatus, StoryType} from 'src/api/types'
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'
  import Button from 'src/components/Button.svelte'
  import Header from 'src/layout/Header.svelte'
  import {onMount} from 'svelte'
  import FormField from 'src/forms/FormField.svelte'
  import {replaceValues} from '@codeborne/i18n-json'
  import ProjectMembersButton from 'src/pages/projects/ProjectMembersButton.svelte'
  import ProjectUpdatesListener from 'src/pages/projects/ProjectUpdatesListener.svelte'
  import ProjectPanel from 'src/pages/stories/ProjectPanel.svelte'
  import ProjectSettingsButton from 'src/pages/projects/ProjectSettingsButton.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'

  const isMobile = innerWidth < 500

  export let id: Id<Project>

  let project: ProjectContext | undefined
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
    project!.tags = [...new Set(stories.flatMap(s => s.tags))]
  }

  async function onSearch(q?: string) {
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
    icebox: !isMobile
  }

  function toggleShow(key: keyof typeof show) {
    if (isMobile) Object.keys(show).forEach(k => k != key && (show[k] = false))
    show[key] = !show[key]
  }

  let pastLoaded = false

  onMount(async () => {
    project = await api.get('projects/' + id)
    velocity = project!.velocity
    api.get<ProjectMemberUser[]>(`projects/${id}/members`).then(r => project!.members = r)
    await loadStories(project!.currentIterationNum)
  })

  $: if (show.done && !pastLoaded) {
    loadStories(0)
    pastLoaded = true
  }

  $: done = stories.filter(s => s.iteration! < project!.currentIterationNum)
  $: icebox = stories.filter(s => s.status === StoryStatus.UNSCHEDULED)
  $: backlog = stories.filter(s => s.status !== StoryStatus.UNSCHEDULED && (!s.iteration || s.iteration >= project!.currentIterationNum))

  async function onDrag(e: {id: Id<Story>, beforeId?: Id<Story>, status?: StoryStatus}) {
    if (e.id == e.beforeId) return
    const fromIndex = stories.findIndex(s => s.id == e.id)
    const story = stories.splice(fromIndex, 1)[0]
    const toIndex = e.beforeId ? stories.findIndex(s => s.id == e.beforeId) : stories.length
    const toStory = stories[toIndex]
    stories.splice(toIndex, 0, story)
    story.order = newOrder(stories[toIndex - 1], stories[toIndex + 1])
    if (e.status) story.status = e.status
    else if (!toStory || toStory.status === StoryStatus.UNSCHEDULED) story.status = StoryStatus.UNSCHEDULED
    else if (story.status === StoryStatus.UNSCHEDULED) story.status = StoryStatus.UNSTARTED
    stories = stories
    stories[toIndex] = await api.post<Story>(`projects/${id}/stories`, story)
  }

  let nextAddIndex: Partial<Record<StoryStatus, number>> = {}
  function addStory(panel: Story[], status: StoryStatus) {
    let index = nextAddIndex[status] ?? stories.findIndex(s => s.type == StoryType.FEATURE && s.status == status)
    if (index < 0) index = panel.length
    const prev = stories[index - 1]
    const next = stories[index]
    const newStory = {
      status, projectId: project!.id, order: newOrder(prev, next),
      type: StoryType.FEATURE, tags: [] as string[], blockers: [] as StoryBlocker[], comments: [] as StoryComment[],
      points: project!.defaultStoryPoints
    } as Story
    stories.splice(index, 0, newStory)
    nextAddIndex[status] = index + 1
    stories = stories
  }

  function newOrder(prev?: Story, next?: Story) {
    const prevOrder = prev?.order ?? 0
    return next ? prevOrder + (next.order - prevOrder) / 2 : Math.ceil(prevOrder + 1)
  }

  function onSaved(story: Story) {
    let index = stories.findIndex(s => s.id == story.id)
    if (index < 0) index = stories.findIndex(s => !s.id)
    if (index >= 0) stories[index] = story
  }

  async function onDelete(story: Story) {
    if (story.id) {
      if (!confirm(replaceValues(t.stories.deleteConfirm, story))) return
      await api.delete(`projects/${id}/stories/${story.id}`)
    }
    let index = stories.findIndex(s => s.id == story.id)
    stories.splice(index, 1)
    stories = stories
  }
</script>

<svelte:head>
  {#if project?.name}
    <title>{project?.name} - {t.title}</title>
  {/if}
</svelte:head>

<div class="h-screen overflow-hidden flex flex-col">
  <Header title={project?.name}>
    {#if project?.members}<ProjectMembersButton {project}/>{/if}
    {#if project}<ProjectSettingsButton {project}/>{/if}
    <FormField type="search" placeholder={t.stories.search.placeholder} on:keydown={e => e.key == 'Enter' && onSearch(e.currentTarget?.['value'])}/>
  </Header>
  <div class="flex px-4" style="height: calc(100vh - 56px)">
    <aside class="w-14 sticky top-0 h-full pt-3 -ml-3">
      <div class="flex flex-col items-center gap-4">
        {#each Object.keys(show) as key}
          <Button icon={key} size="lg" title={t.panels[key]} on:click={() => toggleShow(key)}
                  variant={show[key] ? 'solid' : 'ghost'} color="secondary"/>
        {/each}
      </div>
    </aside>
    {#if !project || !stories}
      <Spinner/>
    {:else}
      <ProjectUpdatesListener {project} bind:stories/>

      <div class="flex gap-2 ml-1 mt-3 w-full">
        <ProjectPanel name="done" bind:show={show.done} {project} stories={done} movable={false} {onSearch} {onSaved} {onDelete}/>

        <ProjectPanel name="backlog" bind:show={show.backlog} {project} {velocity} stories={backlog} status={StoryStatus.UNSTARTED} {onDrag} {onSearch} {onSaved} {onDelete}>
          <button slot="left" title={t.projects.velocity} class="px-2 hover:bg-stone-200" on:click={changeVelocity}>⚡{velocity}</button>
          <Button slot="right" label="＋ {t.stories.add}" on:click={() => addStory(backlog, StoryStatus.UNSTARTED)} variant="outlined"/>
        </ProjectPanel>

        <ProjectPanel name="icebox" bind:show={show.icebox} {project} stories={icebox} status={StoryStatus.UNSCHEDULED} {onDrag} {onSearch} {onSaved} {onDelete}>
          <Button slot="right" label="＋ {t.stories.add}" on:click={() => addStory(icebox, StoryStatus.UNSCHEDULED)} variant="outlined"/>
        </ProjectPanel>

        <ProjectPanel name="search" bind:show={searchQuery} {project} stories={searchResults} movable={false} {onSearch} {onSaved} {onDelete}>
          <span slot="right">{searchQuery}</span>
        </ProjectPanel>
      </div>
    {/if}
  </div>
</div>
