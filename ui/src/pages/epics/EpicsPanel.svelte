<script lang="ts">
  import type {Epic, Story, StoryComment} from 'src/api/types'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {replaceValues} from '@codeborne/i18n-json'
  import api from 'src/api/api'
  import EpicView from 'src/pages/epics/EpicView.svelte'
  import Panel from 'src/components/Panel.svelte'

  export let show: boolean | string | undefined
  export let project: ProjectContext
  export let epics: Epic[] | undefined
  export let stories: Story[]
  export let movable = project.canEdit
  export let onSearch = (tag: string) => {}
  export let onStorySaved: (story: Story) => void = () => {}

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

<Panel bind:show name="epics">
  <span slot="right">
    {#if project.canEdit}
      <Button label="＋ {t.epics.add}" on:click={() => addEpic()} variant="outlined"/>
    {/if}
  </span>

  {#if epics}
    {#each epics as epic, i (epic.id ?? i)}
      <EpicView {project} {epic} {stories} {movable} {onSaved} {onDelete} {onSearch} {onStorySaved}/>
    {/each}
  {:else}
    <Spinner/>
  {/if}
</Panel>
