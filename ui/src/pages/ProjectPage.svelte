<script lang="ts">
  import ProjectPageLayout from 'src/layout/ProjectPageLayout.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import {type Id, type Project, type Story, StoryStatus} from 'src/api/types'
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'
  import Icon from 'src/icons/Icon.svelte'
  import Button from 'src/components/Button.svelte'

  export let id: Id<Project>

  let project: Project | undefined
  let stories: Story[] = []
  let done: Story[] = []

  async function loadProject() {
    project = await api.get('projects/' + id)
  }

  async function loadStories(done: boolean) {
    return await api.get<Story[]>('projects/' + id + '/stories?done=' + done)
  }

  let show: Record<string, boolean> = {
    done: false,
    backlog: true,
    icebox: true
  }

  $: loadProject()
  $: loadStories(false).then(r => stories = r)
  $: if (show.done && !done.length) loadStories(true).then(r => done = r)

  $: icebox = stories.filter(s => s.status === StoryStatus.UNSCHEDULED)
  $: backlog = stories.filter(s => !s.acceptedAt && s.status !== StoryStatus.UNSCHEDULED)
</script>

<ProjectPageLayout title={project?.name}>
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
            <h5 class="panel-title"><Icon name="backlog" size="lg"/> {t.panels.backlog}</h5>
            <StoryList stories={backlog} velocity={project.velocityAveragedWeeks}/>
          </div>
        {/if}
        {#if show.icebox}
          <div class="panel">
            <h5 class="panel-title"><Icon name="icebox" size="lg"/> {t.panels.icebox}</h5>
            <StoryList stories={icebox}/>
          </div>
        {/if}
      </div>
    {/if}
  </div>
</ProjectPageLayout>

<style lang="postcss">
  .panel {
    @apply w-full overflow-y-auto flex flex-col bg-gray-100 border border-gray-200 rounded;
  }

  .panel-title {
    @apply py-2 px-3 text-lg font-semibold sticky top-0 bg-gray-100 border-b;
  }
</style>
