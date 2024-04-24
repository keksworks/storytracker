<script lang="ts">
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import type {Story} from 'src/api/types'
  import {t} from 'src/i18n'

  let project = {
    name: 'My project',
  }

  let stories: Story[] = [
    {title: 'User can get stuff done', description: '', tags: [], points: 2},
    {title: 'Admin can get even more stuff done', description: '', tags: [], points: 3},
  ]
</script>

<MainPageLayout>
  <div class="panels">
    <div class="panel">
      <h5 class="py-1 px-2 text-base">{t.panels.backlog}</h5>
      <StoryList {stories}/>
    </div>
  </div>
</MainPageLayout>

<style>
  .panels {
    @apply flex gap-2
  }

  .panel {
    @apply w-96 overflow-y-auto flex flex-col bg-gray-100
  }
</style>
