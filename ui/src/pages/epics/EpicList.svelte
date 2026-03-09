<script lang="ts">
  import {type Epic, type Story} from 'src/api/types'
  import EpicView from 'src/pages/epics/EpicView.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let project: ProjectContext
  export let epics: Epic[]
  export let stories: Story[]
  export let movable = true
  export let onSaved: (epic: Epic) => void = () => {}
  export let onDelete: (epic: Epic) => void = () => {}
  export let onSearch: (tag: string) => void = () => {}
  export let onStorySaved: (story: Story) => void = () => {}
</script>

{#each epics as epic, i (epic.id ?? i)}
  <EpicView {project} {epic} {stories} {movable} {onSaved} {onDelete} {onSearch} {onStorySaved}/>
{/each}
