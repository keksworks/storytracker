<script lang="ts">
  import type {Project, Story, StoryStatus} from 'src/api/types'
  import {t} from 'src/i18n'
  import Icon from 'src/icons/Icon.svelte'
  import Button from 'src/components/Button.svelte'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import Spinner from 'src/components/Spinner.svelte'

  export let name: keyof typeof t.panels
  export let show: boolean | string | undefined
  export let project: Project
  export let stories: Story[] | undefined
  export let movable = true
  export let velocity = 0
  export let status: StoryStatus | undefined = undefined
</script>

{#if show}
  <div class="panel">
    <h5 class="panel-title">
      <span>
        <Icon {name} size="lg"/> {t.panels[name]}
        <slot name="left"/>
      </span>
      <span>
        <slot name="right"/>
        <Button title={t.general.close} on:click={() => show = undefined} variant="ghost">✕</Button>
      </span>
    </h5>
    {#if stories}
      <StoryList {project} {stories} {status} {velocity} {movable} on:drag on:search on:saved on:delete/>
    {:else}
      <Spinner/>
    {/if}
  </div>
{/if}

<style lang="postcss">
  .panel {
    @apply w-full max-w-6xl overflow-y-auto flex flex-col bg-stone-100 border border-stone-200 rounded;
  }

  .panel-title {
    @apply py-2 px-3 text-lg font-semibold sticky top-0 bg-stone-100 border-b flex justify-between items-center;
  }
</style>
