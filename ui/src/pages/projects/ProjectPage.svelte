<script lang="ts">
  import {type Epic, type Id, type Project, type ProjectMemberUser, Role, type Story, type StoryBlocker, type StoryComment, StoryStatus, StoryType} from 'src/api/types'
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
  import StoryPanel from 'src/pages/stories/StoryPanel.svelte'
  import EpicsPanel from 'src/pages/epics/EpicsPanel.svelte'
  import ProjectHistoryPanel from 'src/pages/projects/ProjectHistoryPanel.svelte'
  import ProjectSettingsButton from 'src/pages/projects/ProjectSettingsButton.svelte'
  import {isMobile, type ProjectContext} from 'src/pages/projects/context'
  import {user} from 'src/stores/auth'
  import ProjectExportButton from 'src/pages/projects/ProjectExportButton.svelte'

  export let id: Id<Project>

  let project: ProjectContext | undefined
  let stories: Story[] = []
  let epics: Epic[] = []
  let searchQuery: string | undefined
  let searchResults: Story[] | undefined
  let velocity = 10
  let highlightStoryId: number | undefined
  let flashStoryId: number | undefined

  function changeVelocity() {
    const v = parseInt(prompt(t.projects.velocityOverride, velocity.toString())!)
    if (v) velocity = v
  }

  async function loadStories(fromIteration: number) {
    stories = await api.get<Story[]>(`projects/${id}/stories?fromIteration=${fromIteration}`)
    if (!fromIteration) pastLoaded = true
  }

  let show: Record<string, boolean> = {
    done: false,
    backlog: true,
    icebox: !isMobile,
    epics: false,
    history: false
  }

  Object.entries(JSON.parse(localStorage['projectPanels:' + id] || '{}')).forEach(e => {
    if (typeof e[1] === 'boolean') show[e[0]] = e[1]
  })

  $: localStorage['projectPanels:' + id] = JSON.stringify(show)

  function hideAll(key?: keyof typeof show) {
    Object.keys(show).forEach(k => k != key && (show[k] = false))
  }

  function toggleShow(key: keyof typeof show) {
    if (isMobile) hideAll(key)
    show[key] = !show[key]
  }

  async function onSearch(q?: string) {
    searchQuery = q
    searchResults = undefined
    if (q) {
      if (isMobile) hideAll()
      searchResults = await api.get<Story[]>(`projects/${id}/stories?q=${encodeURIComponent(q)}`)
    } else {
      searchQuery = undefined
      if (isMobile) show.backlog = true
    }
  }

  function onLocate(story: Story) {
    if (story.status === StoryStatus.UNSCHEDULED) show.icebox = true
    else show.backlog = true
    highlightStoryId = story.id
  }

  let pastLoaded = false
  const initialOpenStoryId = parseInt(location.hash.substring(1))

  onMount(async () => {
    project = await api.get('projects/' + id)
    velocity = project!.velocity
    api.get<ProjectMemberUser[]>(`projects/${id}/members`).then(r => {
      project!.members = r.indexBy(m => m.user.id)
      const role = project?.members[$user.id]?.member?.role
      project!.isOwner = $user.isAdmin || role == Role.OWNER
      project!.canEdit = project!.isOwner || role == Role.MEMBER
    })
    api.get<Epic[]>(`projects/${id}/epics`).then(r => epics = r)
    await loadStories(show.done ? 0 : project!.currentIterationNum)
    if (initialOpenStoryId && !stories.find(s => s.id === initialOpenStoryId)) await onSearch(location.hash)
  })

  $: if (project) project.epicTags = new Set(epics.map(e => e.tag))
  $: if (project) project.tags = [...new Set(stories.flatMap(s => s.tags).concat([...project.epicTags ?? []]))]

  $: if (show.done && !pastLoaded) loadStories(0)

  $: done = stories.filter(s => s.iteration! < project?.currentIterationNum!)
  $: icebox = stories.filter(s => s.status === StoryStatus.UNSCHEDULED)
  $: backlog = stories.filter(s => s.status !== StoryStatus.UNSCHEDULED && (!s.iteration || s.iteration >= project?.currentIterationNum!))

  async function onDrag(e: {id: Id<Story>, beforeId?: Id<Story>, status?: StoryStatus}) {
    if (!e.id || e.id == e.beforeId) return
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
      status, projectId: project!.id, createdBy: $user.id,
      order: newOrder(prev, next),
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

<div class="h-screen sm:overflow-hidden flex flex-col">
  <Header title={project?.name}>
    {#if project?.members}
      <ProjectMembersButton {project}/>
      <ProjectSettingsButton {project}/>
      <ProjectExportButton {project}/>
    {/if}
    {#if !isMobile}{@render search()}{/if}
  </Header>
  <div class="flex px-4 max-sm:flex-col max-sm:!h-auto" style="height: calc(100vh - 56px)">
    <aside class="w-14 max-sm:w-full sm:h-full pt-3 sm:-ml-3">
      <div class="flex sm:flex-col items-center gap-4">
        {#each Object.keys(show) as key}
          <Button icon={key} size={isMobile ? '' : 'lg'} title={t.panels[key]} on:click={() => toggleShow(key)}
                  variant={show[key] ? 'solid' : 'ghost'} color="secondary"/>
        {/each}
        {#if isMobile}{@render search()}{/if}
      </div>
    </aside>
    {#if !project || !project.members || !stories}
      <Spinner/>
    {:else}
      <ProjectUpdatesListener {project} bind:stories bind:epics onStoryUpdated={s => flashStoryId = s.id}/>

      <div class="flex gap-2 ml-1 mt-3 w-full">
        <StoryPanel name="done" bind:show={show.done} {project} stories={done} movable={false}
                    {onSearch} {onSaved} {onDelete} bind:flashStoryId
                    collapseStory={s => s.iteration! < project!.currentIterationNum - 3 && s.id !== initialOpenStoryId}/>

        <StoryPanel name="backlog" bind:show={show.backlog} {project} {velocity} stories={backlog} status={StoryStatus.UNSTARTED} {onDrag} {onSearch} {onSaved} {onDelete} bind:highlightStoryId bind:flashStoryId>
          <button slot="left" title={t.projects.velocity} class="px-2 hover:bg-stone-200" on:click={changeVelocity}>⚡{velocity}</button>
          <span slot="right">
            {#if project.canEdit}
              <Button label="＋ {t.stories.add}" on:click={() => addStory(backlog, StoryStatus.UNSTARTED)} variant="outlined"/>
            {/if}
          </span>
       </StoryPanel>

        <StoryPanel name="icebox" bind:show={show.icebox} {project} stories={icebox} status={StoryStatus.UNSCHEDULED} {onDrag} {onSearch} {onSaved} {onDelete} bind:highlightStoryId bind:flashStoryId>
          <span slot="right">
            {#if project.canEdit}
              <Button slot="right" label="＋ {t.stories.add}" on:click={() => addStory(icebox, StoryStatus.UNSCHEDULED)} variant="outlined"/>
            {/if}
          </span>
        </StoryPanel>

        <EpicsPanel bind:show={show.epics} {project} bind:epics {stories} {onSearch} onStorySaved={onSaved}/>

        <StoryPanel name="search" bind:show={searchQuery} {project} stories={searchResults} movable={false}
                    {onSearch} {onSaved} {onDelete} {onLocate} bind:flashStoryId
                    collapseStory={s => s.iteration! < project!.currentIterationNum && s.id !== initialOpenStoryId}>
          <span slot="right">{searchQuery}</span>
        </StoryPanel>

        <ProjectHistoryPanel bind:show={show.history} {project} {stories} {epics}/>
      </div>
    {/if}
  </div>
</div>

{#snippet search()}
  <FormField type="search" placeholder={t.stories.search.placeholder}
             on:keydown={e => e.key == 'Enter' && onSearch(e.currentTarget?.['value'])}
             on:input={e => isMobile && !e['value'] && onSearch()}
  />
{/snippet}
