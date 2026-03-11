<script lang="ts">
  import type {ChangeHistory} from 'src/api/types'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Panel from 'src/components/Panel.svelte'
  import api from 'src/api/api'
  import {t} from 'src/i18n'

  export let show: boolean
  export let project: ProjectContext

  let history: ChangeHistory[] = []

  $: if (show) loadHistory()

  async function loadHistory() {
    history = await api.get<ChangeHistory[]>(`projects/${project.id}/history`)
  }

  function formatValue(val: string | undefined) {
    if (val === undefined || val === null) return '—'
    if (val === '') return '""'
    return val
  }

  function getUserName(id: number | undefined) {
    if (!id) return 'System'
    return project.members[id]?.user.name ?? id
  }
</script>

<Panel name="history" bind:show>
  <div class="p-3">
    {#each history as item}
      <div class="mb-2 pb-2 border-b border-stone-300 last:border-0">
        <div class="flex justify-between text-xs text-stone-500 mb-1">
          <span>{new Date(item.changedAt).toLocaleString()}</span>
          <span class="font-medium text-stone-600">{getUserName(item.changedBy)}</span>
        </div>
        <div class="text-sm">
          <span class="text-stone-500">{item.table} #{item.rowId}</span>: 
          <strong>{item.column}</strong> 
          {#if item.oldValue !== item.newValue}
            <span class="text-stone-400 mx-1">→</span> 
            <span class="line-through text-stone-400">{formatValue(item.oldValue)}</span> 
            <span class="font-medium text-stone-800">{formatValue(item.newValue)}</span>
          {/if}
        </div>
      </div>
    {:else}
      <div class="text-stone-500 text-center py-8">{t.general.noItems}</div>
    {/each}
  </div>
</Panel>
