<script lang="ts">
  import {type Epic} from 'src/api/types'
  import EpicView from 'src/pages/epics/EpicView.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let project: ProjectContext
  export let epics: Epic[]
  export let movable = true
  export let onSaved: (epic: Epic) => void = () => {}
  export let onDelete: (epic: Epic) => void = () => {}
</script>

{#each epics as epic, i (epic.id ?? i)}
  <EpicView {project} {epic} {movable} {onSaved} {onDelete}/>
{/each}
