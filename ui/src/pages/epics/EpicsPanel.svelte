<script lang="ts">
  import type {Epic, Story, StoryComment} from 'src/api/types'
  import {t} from 'src/i18n'
  import Icon from 'src/icons/Icon.svelte'
  import Button from 'src/components/Button.svelte'
  import EpicList from 'src/pages/epics/EpicList.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {replaceValues} from '@codeborne/i18n-json'
  import api from 'src/api/api'

  export let name: keyof typeof t.panels
  export let show: boolean | string | undefined
  export let project: ProjectContext
  export let epics: Epic[] | undefined
  export let stories: Story[]
  export let movable = project.canEdit
  export let onSearch = (tag: string) => {}

  function addEpic() {
    epics = [{
      projectId: project!.id,
      comments: [] as StoryComment[],
    } as Epic, ...epics!]
  }

  function onSaved(epic: Epic) {
    let index = epics!.findIndex(e => e.id == epic.id)
    if (index < 0) index = epics!.findIndex(e => !e.id)
    if (index >= 0) epics![index] = epic
  }

  async function onDelete(epic: Epic) {
    if (epic.id) {
      if (!confirm(replaceValues(t.epics.deleteConfirm, epic))) return
      await api.delete(`projects/${project.id}/epics/${epic.id}`)
    }
    let index = epics!.findIndex(e => e.id == epic.id)
    epics!.splice(index, 1)
    epics = epics
  }
</script>

{#if show}
  <div class="panel">
    <h5 class="panel-title">
      <span>
        <Icon {name} size="lg"/> {t.panels[name]}
        <slot name="left"/>
      </span>
      <span>
        {#if project.canEdit}
          <Button label="＋ {t.epics.add}" on:click={() => addEpic()} variant="outlined"/>
        {/if}
        <Button title={t.general.close} on:click={() => show = undefined} variant="ghost" class="max-sm:!hidden">✕</Button>
      </span>
    </h5>
    {#if epics}
      <EpicList {project} {epics} {stories} {movable} {onSaved} {onDelete} {onSearch}/>
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
