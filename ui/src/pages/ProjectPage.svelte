<script lang="ts">
  import ProjectPageLayout from 'src/layout/ProjectPageLayout.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import type {Story} from 'src/api/types'
  import {t} from 'src/i18n'

  let project = {
    name: 'My project',
    velocity: 10,
  }

  let stories: Story[] = [
    {id: '1', title: 'User can get stuff done', description: '* We need a lot of stuff\n* To be done\n* Maybe it should be split', tags: ['user', 'stuff'], points: 2, createdAt: '2024-04-19T10:22'},
    {id: '2', title: 'Admin can get even more stuff done', description: '', tags: ['admin'], points: 3, createdAt: '2024-03-28T18:35'},
  ]

  let backlog = Array(10).fill(0).flatMap(() => stories).map((s, i) => ({...s, id: (i+1).toString()}))
  let icebox = Array(10).fill(0).flatMap(() => stories).map((s, i) => ({...s, id: (i+1).toString()}))
</script>

<ProjectPageLayout title={project.name}>
  <div class="panels mt-3 w-full">
    <div class="panel">
      <h5 class="panel-title">{t.panels.backlog}</h5>
      <StoryList stories={backlog} velocity={project.velocity}/>
    </div>
    <div class="panel">
      <h5 class="panel-title">{t.panels.icebox}</h5>
      <StoryList stories={icebox} velocity={project.velocity}/>
    </div>
  </div>
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
