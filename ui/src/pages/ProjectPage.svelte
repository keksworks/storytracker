<script lang="ts">
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import {type Id, type Project, type Story, StoryStatus} from 'src/api/types'
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
  let searchResults: Story[] | undefined
  let velocity = 10

  function changeVelocity() {
    const v = parseInt(prompt(t.projects.velocityOverride, velocity.toString())!)
    if (v) velocity = v
  }

  async function loadStories(fromIteration: number) {
    stories = await api.get<Story[]>(`projects/${id}/stories?fromIteration=${fromIteration}`)
  }

  async function search(e: FormEvent) {
    const q = e.currentTarget.value
    if (q) {
      show.search = true
      searchResults = await api.get<Story[]>(`projects/${id}/stories?q=${q}`)
    } else {
      show.search = false
    }
  }

  let show: Record<string, boolean> = {
    done: false,
    backlog: true,
    icebox: true,
    search: false
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
</script>

<svelte:head>
  {#if project?.name}
    <title>{project?.name} - {t.title}</title>
  {/if}
</svelte:head>

<div class="h-screen overflow-hidden flex flex-col">
  <Header title={project?.name}>
    <FormField type="search" placeholder={t.stories.search.placeholder} on:change={search}/>
  </Header>
  <div class="flex h-screen px-4">
    <aside class="w-16 sticky top-0 h-screen pt-6">
      <div class="flex flex-col items-center gap-4">
        {#each Object.keys(show) as key}
          <Button icon={key} size="lg" on:click={() => show[key] = !show[key]} variant={show[key] ? 'outlined' : 'ghost'}/>
        {/each}
      </div>
    </aside>
    {#if !project || !stories}
      <Spinner/>
    {:else}
      <div class="grid gap-2 ml-1 mt-3 w-full grid-cols-{Object.values(show).filter(v=>!!v).length}">
        {#if show.done}
          <div class="panel">
            <h5 class="panel-title"><Icon name="done" size="lg"/> {t.panels.done}</h5>
            <StoryList stories={done}/>
          </div>
        {/if}
        {#if show.backlog}
          <div class="panel">
            <h5 class="panel-title">
              <Icon name="backlog" size="lg"/>
              {t.panels.backlog}
              <button title={t.projects.velocity} class="px-2 hover:bg-stone-200" on:click={changeVelocity}>⚡{velocity}</button>
            </h5>
            <StoryList stories={backlog} {velocity}/>
          </div>
        {/if}
        {#if show.icebox}
          <div class="panel">
            <h5 class="panel-title"><Icon name="icebox" size="lg"/> {t.panels.icebox}</h5>
            <StoryList stories={icebox}/>
          </div>
        {/if}
        {#if searchResults}
          <div class="panel">
            <h5 class="panel-title"><Icon name="search" size="lg"/> {t.stories.search.title}</h5>
            <StoryList stories={searchResults} movable={false}/>
          </div>
        {/if}
      </div>
    {/if}
  </div>
</div>

<style lang="postcss">
  .panel {
    @apply w-full overflow-y-auto flex flex-col bg-gray-100 border border-gray-200 rounded;
  }

  .panel-title {
    @apply py-2 px-3 text-lg font-semibold sticky top-0 bg-gray-100 border-b;
  }
</style>
