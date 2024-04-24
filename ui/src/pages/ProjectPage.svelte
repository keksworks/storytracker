<script lang="ts">
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import type {Story} from 'src/api/types'
  import {t} from 'src/i18n'

  let project = {
    name: 'My project',
  }

  let stories: Story[] = [
    {id: '1', title: 'User can get stuff done', description: '* We need a lot of stuff\n* To be done\n* Maybe it should be split', tags: ['user', 'stuff'], points: 2, createdAt: '2024-04-19T10:22'},
    {id: '2', title: 'Admin can get even more stuff done', description: '', tags: ['admin'], points: 3, createdAt: '2024-03-28T18:35'},
  ]

  let backlog = Array(10).fill(0).flatMap(() => stories).map((s, i) => ({...s, id: (i+1).toString()}))
</script>

<MainPageLayout>
  <div class="panels">
    <div class="panel">
      <h5 class="py-1 px-2 text-base">{t.panels.backlog}</h5>
      <StoryList stories={backlog}/>
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
