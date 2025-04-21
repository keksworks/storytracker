<script lang="ts">
  import ProjectPageLayout from 'src/layout/ProjectPageLayout.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import {type Id, type Project, type Story, StoryStatus} from 'src/api/types'
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'

  export let id: Id<Project>

  let project: Project | undefined
  let stories: Story[] = []

  async function load() {
    project = await api.get('projects/' + id)
    stories = await api.get('projects/' + id + '/stories')
  }

  $: load()

  $: icebox = stories.filter(s => s.status === StoryStatus.UNSCHEDULED)
  $: backlog = stories.filter(s => !s.acceptedAt && s.status !== StoryStatus.UNSCHEDULED)
</script>

<ProjectPageLayout title={project?.name}>
  {#if !project || !stories}
    <Spinner/>
  {:else}
    <div class="panels mt-3 w-full">
      <div class="panel">
        <h5 class="panel-title">{t.panels.backlog}</h5>
        <StoryList stories={backlog} velocity={project.velocityAveragedWeeks}/>
      </div>
      <div class="panel">
        <h5 class="panel-title">{t.panels.icebox}</h5>
        <StoryList stories={icebox} velocity={project.velocityAveragedWeeks}/>
      </div>
    </div>
  {/if}
</ProjectPageLayout>

<style>
  .panels {
    @apply grid grid-cols-2 gap-2
  }

  .panel {
    @apply w-full overflow-y-auto flex flex-col bg-gray-100 border border-gray-200 rounded
  }

  .panel-title {
    @apply py-2 px-3 text-lg font-semibold sticky top-0 bg-gray-100 border-b
  }
</style>
