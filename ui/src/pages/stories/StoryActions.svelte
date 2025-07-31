<script lang="ts">
  import {type Instant, type Story, StoryStatus, StoryType} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import {t} from 'src/i18n'

  export let story: Story
  export let save: (move?: boolean) => void
  export let open: boolean

  const size = 'sm'

  function start(e: Event) {
    story.status = StoryStatus.STARTED
    justSave(e, true)
  }

  function finish(e: Event) {
    story.status = StoryStatus.FINISHED
    justSave(e)
  }

  function deliver(e: Event) {
    story.status = StoryStatus.DELIVERED
    justSave(e)
  }

  function accept(e: Event) {
    story.status = StoryStatus.ACCEPTED
    story.acceptedAt = new Date().toISOString() as Instant
    justSave(e, true)
  }

  function reject(e: Event) {
    story.status = StoryStatus.REJECTED
    justSave(e)
  }

  function justSave(e: Event, move?: boolean) {
    e.stopPropagation()
    save(move)
  }
</script>

<div title={t.stories.statuses[story.status]}>
  {#if open}
    <Button {size} color="primary" variant="soft" on:click={justSave}>{t.general.save}</Button>
  {:else if story.status === StoryStatus.UNSTARTED && story.type !== StoryType.RELEASE}
    <Button {size} color="secondary" variant="soft" on:click={start}>{t.stories.actions.start}</Button>
  {:else if story.status === StoryStatus.STARTED}
    <Button {size} color="secondary" on:click={finish}>{t.stories.actions.finish}</Button>
  {:else if story.status === StoryStatus.DELIVERED}
    <div class="flex gap-2">
      <Button {size} color="success" on:click={accept}>{t.stories.actions.accept}</Button>
      <Button {size} color="danger" on:click={reject}>{t.stories.actions.reject}</Button>
    </div>
  {:else if story.status === StoryStatus.FINISHED}
    <Button {size} color="warning" on:click={deliver}>{t.stories.actions.deliver}</Button>
  {:else if story.status === StoryStatus.REJECTED}
    <Button {size} color="secondary" variant="soft" on:click={start}>
      <div class="flex items-center space-x-2">
        <span class="w-3 h-3 rounded-full bg-red-700"/>
        <span>{t.stories.actions.restart}</span>
      </div>
    </Button>
  {/if}
</div>
