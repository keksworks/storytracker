<script lang="ts">
  import {StoryStatus, StoryType} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import {t} from 'src/i18n'

  export let status: StoryStatus
  export let type: StoryType

  const size = 'sm'
</script>
{#if status === StoryStatus.UNSTARTED && type !== StoryType.RELEASE}
  <Button {size} color="secondary" variant="soft">{t.storyActions.start}</Button>
{:else if status === StoryStatus.STARTED}
  <Button {size} color="secondary">{t.storyActions.finish}</Button>
{:else if status === StoryStatus.DELIVERED}
  <div class="flex gap-2">
    <Button {size} color="success">{t.storyActions.accept}</Button>
    <Button {size} color="danger">{t.storyActions.reject}</Button>
  </div>
{:else if status === StoryStatus.FINISHED}
  <Button {size} color="warning">{t.storyActions.deliver}</Button>
{:else if status === StoryStatus.REJECTED}
  <Button {size} color="secondary" variant="soft">
    <div class="flex items-center space-x-2">
      <span class="w-3 h-3 rounded-full bg-red-700"/>
      <span>{t.storyActions.restart}</span>
    </div>
  </Button>
{/if}
