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

  async function load() {
    project = await api.get('projects/' + id)
    stories = await api.get('projects/' + id + '/stories')
  }

  $: load()

  let show = {
    done: false,
    backlog: true,
    icebox: true
  }

  $: done = stories.filter(s => s.acceptedAt)
  $: icebox = stories.filter(s => s.status === StoryStatus.UNSCHEDULED)
  $: backlog = stories.filter(s => !s.acceptedAt && s.status !== StoryStatus.UNSCHEDULED)
</script>

<ProjectPageLayout title={project?.name}>
  <div class="relative flex flex-grow-0 overflow-hidden px-2 sm:px-3 w-full">
    <div class="w-16 absolute flex flex-col items-center pt-6 gap-6">
      {#each Object.keys(show) as key}
        <Button icon={key} size="lg" on:click={() => show[key] = !show[key]} variant="ghost"/>
      {/each}
    </div>
    {#if !project || !stories}
      <Spinner/>
    {:else}
      <div class="grid grid-cols-3 gap-2 ml-16 mt-3 w-full">
        {#if show.done}
          <div class="panel">
            <h5 class="panel-title"><Icon name="done" size="lg"/> {t.panels.done}</h5>
            <StoryList stories={done} velocity={project.velocityAveragedWeeks}/>
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
            <StoryList stories={icebox} velocity={project.velocityAveragedWeeks}/>
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
