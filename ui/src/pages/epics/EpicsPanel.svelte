<script lang="ts">
  import type {Epic, Id, Story, StoryComment} from 'src/api/types'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {replaceValues} from '@codeborne/i18n-json'
  import api from 'src/api/api'
  import EpicView from 'src/pages/epics/EpicView.svelte'
  import Panel from 'src/components/Panel.svelte'
  import {user} from 'src/stores/auth'
  import {stopAutoscroll} from 'src/shared/autoscroll'
  import {dragged} from 'src/shared/draggable'

  export let show: boolean
  export let project: ProjectContext
  export let epics: Epic[] | undefined
  export let stories: Story[]
  export let movable = project.canEdit
  export let onSearch = (tag: string) => {}
  export let onStorySaved: (story: Story) => void = () => {}

  function addEpic() {
    epics = [{
      projectId: project!.id, createdBy: $user.id,
      order: newOrder(undefined, epics?.[0]),
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

  async function onDrag(e: {id: Id<Epic>, beforeId?: Id<Epic>}) {
    if (e.id == e.beforeId) return
    const fromIndex = epics!.findIndex(s => s.id == e.id)
    const epic = epics!.splice(fromIndex, 1)[0]
    const toIndex = e.beforeId ? epics!.findIndex(s => s.id == e.beforeId) : epics!.length
    epics!.splice(toIndex, 0, epic)
    epic.order = newOrder(epics![toIndex - 1], epics![toIndex + 1])
    epics = epics
    epics![toIndex] = await api.post<Epic>(`projects/${project.id}/epics`, epic)
  }

  function newOrder(prev?: Epic, next?: Epic) {
    const prevOrder = prev?.order ?? 0
    return next ? prevOrder + (next.order - prevOrder) / 2 : Math.ceil(prevOrder + 1)
  }

  let isDropTarget = false
  function onDropAtEnd() {
    if (dragged.type === 'epic') onDrag({id: dragged.id as Id<Epic>})
    isDropTarget = false
  }
</script>

<Panel bind:show name="epics" class="epics">
  <span slot="right">
    {#if project.canEdit}
      <Button label="＋ {t.epics.add}" on:click={() => addEpic()} variant="outlined"/>
    {/if}
  </span>

  {#if epics}
    {#each epics as epic, i (epic.id ?? i)}
      <EpicView {project} {epic} {stories} {movable} {onSaved} {onDelete} {onSearch} {onStorySaved} {onDrag}/>
    {/each}

    {#if movable}
      <div class="min-h-16 {isDropTarget ? 'drop-order' : ''}" draggable={movable} ondrop={onDropAtEnd}
           ondragover={e => {e.preventDefault(); isDropTarget = true}}
           ondragleave={() => {isDropTarget = false; stopAutoscroll()}}
           role="region" aria-label={t.epics.add}>
      </div>
    {/if}
  {:else}
    <Spinner/>
  {/if}
</Panel>
