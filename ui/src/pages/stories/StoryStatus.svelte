<script lang="ts">
  import {type Story, StoryStatus, StoryType} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import {t} from 'src/i18n'

  export let story: Story

  const size = 'sm'

  function start(e: Event) {
    e.stopPropagation()
    story.status = StoryStatus.STARTED
  }

  function finish(e: Event) {
    e.stopPropagation()
    story.status = StoryStatus.FINISHED
  }

  function deliver(e: Event) {
    e.stopPropagation()
    story.status = StoryStatus.DELIVERED
  }

  function accept(e: Event) {
    e.stopPropagation()
    story.status = StoryStatus.ACCEPTED
  }

  function reject(e: Event) {
    e.stopPropagation()
    story.status = StoryStatus.REJECTED
  }
</script>

{#if story.status === StoryStatus.UNSTARTED && story.type !== StoryType.RELEASE}
  <Button {size} color="secondary" variant="soft" on:click={start}>{t.storyActions.start}</Button>
{:else if story.status === StoryStatus.STARTED}
  <Button {size} color="secondary" on:click={finish}>{t.storyActions.finish}</Button>
{:else if story.status === StoryStatus.DELIVERED}
  <div class="flex gap-2">
    <Button {size} color="success" on:click={accept}>{t.storyActions.accept}</Button>
    <Button {size} color="danger" on:click={reject}>{t.storyActions.reject}</Button>
  </div>
{:else if story.status === StoryStatus.FINISHED}
  <Button {size} color="warning" on:click={deliver}>{t.storyActions.deliver}</Button>
{:else if story.status === StoryStatus.REJECTED}
  <Button {size} color="secondary" variant="soft" on:click={start}>
    <div class="flex items-center space-x-2">
      <span class="w-3 h-3 rounded-full bg-red-700"/>
      <span>{t.storyActions.restart}</span>
    </div>
  </Button>
{/if}
