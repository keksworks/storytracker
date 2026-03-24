<script lang="ts">
  import {type Story, StoryStatus, StoryType} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import {t} from 'src/i18n'
  import {user} from 'src/stores/auth'
  import {onStatusChanged} from './status'

  export let story: Story
  export let save: (move?: boolean) => void
  export let open: boolean
  export let disabled: boolean
  export let onLocate: ((story: Story) => void) | undefined = undefined

  const size = 'sm'

  function start(e: Event) {
    story.status = StoryStatus.STARTED
    story.assignedTo = $user.id
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
    justSave(e, true)
  }

  function reject(e: Event) {
    story.status = StoryStatus.REJECTED
    justSave(e)
  }

  function justSave(e: Event, move?: boolean) {
    e.stopPropagation()
    onStatusChanged(story)
    save(move)
  }
</script>

<div title={t.stories.statuses[story.status]} class="status">
  {#if open}
    <Button {size} color="primary" variant="soft" on:click={justSave} {disabled}>{t.general.save}</Button>
  {:else if onLocate}
    <Button label="⌖" variant="ghost" size="sm" class="!text-2xl" on:click={e => {e.stopPropagation(); onLocate!(story)}} title={t.stories.locate}/>
  {:else if story.status === StoryStatus.UNSTARTED && story.type !== StoryType.RELEASE}
    <Button {size} color="secondary" variant="soft" on:click={start} {disabled}>{t.stories.actions.start}</Button>
  {:else if story.status === StoryStatus.STARTED}
    <Button {size} color="secondary" on:click={finish} {disabled}>{t.stories.actions.finish}</Button>
  {:else if story.status === StoryStatus.DELIVERED}
    <div class="flex gap-2 items-start">
      <Button {size} color="success" on:click={accept} {disabled}>{t.stories.actions.accept}</Button>
      <Button {size} color="danger" on:click={reject} {disabled}>{t.stories.actions.reject}</Button>
    </div>
  {:else if story.status === StoryStatus.FINISHED}
    <Button {size} color="warning" on:click={deliver} {disabled}>{t.stories.actions.deliver}</Button>
  {:else if story.status === StoryStatus.REJECTED}
    <Button {size} color="secondary" variant="soft" on:click={start} {disabled}>
      <div class="flex items-center space-x-2">
        <span class="w-3 h-3 rounded-full bg-red-700"></span>
        <span>{t.stories.actions.restart}</span>
      </div>
    </Button>
  {/if}
</div>

<style lang="postcss">
  .status :global(.btn) {
    @apply whitespace-nowrap;
  }
</style>
