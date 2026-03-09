<script lang="ts">
  import type {Epic} from 'src/api/types'
  import {t} from 'src/i18n'
  import Icon from 'src/icons/Icon.svelte'
  import Button from 'src/components/Button.svelte'
  import EpicsList from 'src/pages/epics/EpicsList.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let name: keyof typeof t.panels
  export let show: boolean | string | undefined
  export let project: ProjectContext
  export let epics: Epic[] | undefined
  export let movable = project.canEdit
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
        <Button title={t.general.close} on:click={() => show = undefined} variant="ghost" class="max-sm:!hidden">✕</Button>
      </span>
    </h5>
    {#if epics}
      <EpicsList {project} {epics} {movable} {...$$restProps}/>
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
