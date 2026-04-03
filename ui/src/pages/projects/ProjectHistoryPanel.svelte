<script lang="ts">
  import type {Change, Epic, Id, Story, User} from 'src/api/types'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Panel from 'src/components/Panel.svelte'
  import api from 'src/api/api'
  import {t} from 'src/i18n'

  export let show: boolean
  export let project: ProjectContext
  export let stories: Story[]
  export let epics: Epic[]

  let history: Change[] = []

  $: if (show) loadHistory()

  async function loadHistory() {
    history = await api.get<Change[]>(`projects/${project.id}/history`)
  }

  function formatValue(val: string | undefined, column: string) {
    if (val === undefined || val === null) return '—'
    if (val === '') return '""'
    if (column === 'status') return t.stories.statuses[val] ?? val
    if (column === 'type') return t.stories.types[val] ?? val
    if (column === 'role') return t.users.roles[val] ?? val
    if (column === 'assignedto') return getUserName(Number(val))
    return val
  }

  function getUserName(id: number | undefined) {
    if (!id) return t.history.system
    return project.members[id as Id<User>]?.user.name ?? id
  }

  function getEntityName(item: Change) {
    if (item.table === 'stories') return stories.find(s => s.id === item.rowId)?.name || `#${item.rowId}`
    if (item.table === 'epics') return epics.find(e => e.id === item.rowId)?.name || `#${item.rowId}`
    if (item.table === 'projects') return project.name
    if (item.table === 'project_members') {
      const m = Object.values(project.members).find(m => m.id === item.rowId)
      return m?.user.name || `#${item.rowId}`
    }
    return `#${item.rowId}`
  }

  function formatTable(table: string) {
    if (table === 'project_members') return t.projects.member
    if (table === 'projects') return t.projects.project
    if (table === 'stories') return t.stories.story
    if (table === 'epics') return t.epics.epic
    return table
  }

  function getDiff(oldVal?: string, newVal?: string) {
    const oldArr = parseArray(oldVal)
    const newArr = parseArray(newVal)
    return {
      added: newArr.filter(x => !oldArr.includes(x)),
      removed: oldArr.filter(x => !newArr.includes(x))
    }
  }

  function parseArray(val?: string): string[] {
    try {
      const parsed = JSON.parse(val || '[]')
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  function parseComments(val?: string): any[] {
    try {
      const parsed = JSON.parse(val || '[]')
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  function getCommentDiff(oldValue?: string, newValue?: string) {
    const oldComments = parseComments(oldValue)
    const newComments = parseComments(newValue)
    if (newComments.length > oldComments.length) return {type: 'added', comment: newComments[newComments.length - 1]}
    if (newComments.length < oldComments.length) return {type: 'deleted'}

    for (let i = 0; i < newComments.length; i++) {
      if (JSON.stringify(newComments[i]) !== JSON.stringify(oldComments[i])) {
        return {type: 'changed', comment: newComments[i]}
      }
    }
    return {type: 'unknown'}
  }
</script>

<Panel name="history" bind:show>
  <div class="p-3">
    {#each history as item}
      <div class="mb-2 pb-2 border-b border-stone-300 last:border-0">
        <div class="flex justify-between text-xs text-stone-500 mb-1">
          <span class="font-medium text-stone-600">{getUserName(item.changedBy)}</span>
          <span>{new Date(item.changedAt).toLocaleString()}</span>
        </div>
        <div class="text-sm">
          <span class="text-stone-500"><i>{formatTable(item.table)}</i> {getEntityName(item)}</span>:
          {#each Object.keys(item.new) as column}
            {@const oldValue = item.old[column]}
            {@const newValue = item.new[column]}
            {#if column === 'ord'}
              <span class="ml-1 text-stone-600 italic">
                {Number(newValue) > Number(oldValue) ? t.history.movedDown : t.history.movedUp}
              </span>
            {:else if column === 'tags'}
              {@const diff = getDiff(oldValue, newValue)}
              {#each diff.added as tag}
                <span class="ml-1 px-1 bg-green-100 text-green-800 rounded text-xs">+{tag}</span>
              {/each}
              {#each diff.removed as tag}
                <span class="ml-1 px-1 bg-red-100 text-red-800 rounded text-xs line-through text-opacity-50">-{tag}</span>
              {/each}
            {:else if column === 'comments'}
              {@const diff = getCommentDiff(oldValue, newValue)}
              <span class="ml-1 text-stone-600 italic">
                {#if diff.type === 'added'}
                  {t.history.added}: "{diff.comment?.text || '...'}"
                {:else if diff.type === 'deleted'}
                  {t.history.deleted}
                {:else if diff.type === 'changed'}
                  {t.history.changed}: "{diff.comment?.text || '...'}"
                {:else}
                  ...
                {/if}
              </span>
            {:else if oldValue !== newValue}
              <span class="ml-1"><strong>{column}</strong></span>
              <span class="text-stone-400 mx-1">→</span>
              <span class="line-through text-stone-400">{formatValue(oldValue, column)}</span>
              <span class="font-medium text-stone-800">{formatValue(newValue, column)}</span>
            {/if}
          {:else}
            <span class="text-stone-600 italic">{t.general.created}</span>
          {/each}
        </div>
      </div>
    {:else}
      <div class="text-stone-500 text-center py-8">{t.general.noItems}</div>
    {/each}
  </div>
</Panel>
