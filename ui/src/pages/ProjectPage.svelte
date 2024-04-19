<script lang="ts">
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import type {Story} from 'src/api/types'

  let project = {
    name: 'My project',
  }

  let stories: Story[] = [
    {title: 'User can get stuff done', description: '', tags: [], points: 2},
    {title: 'Admin can get even more stuff done', description: '', tags: [], points: 3},
  ]
</script>

<MainPageLayout>
  <div class="columns">
    <div class="column">
      <StoryList {stories}/>
    </div>
  </div>
</MainPageLayout>

<style>
  .columns {
    @apply flex gap-2
  }

  .column {
    @apply max-w-48 border overflow-y-auto flex flex-col
  }
</style>
