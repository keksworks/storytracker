<script lang="ts">
  import type {Iteration} from 'src/api/types'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Panel from 'src/components/Panel.svelte'
  import api from 'src/api/api'
  import {formatDate, t} from 'src/i18n'
  import Spinner from 'src/components/Spinner.svelte'

  export let show: boolean
  export let project: ProjectContext

  let iterations: Iteration[] | undefined

  $: if (show && !iterations) loadIterations()

  async function loadIterations() {
    iterations = await api.get<Iteration[]>(`projects/${project.id}/iterations`)
  }

  $: past = (iterations ?? []).filter(it => it.number < project.currentIterationNum)
  $: maxPoints = Math.max(...past.map(it => it.acceptedPoints ?? 0), 1)
</script>

<Panel name="velocity" bind:show>
  <div class="p-4 overflow-x-auto">
    {#if !iterations}
      <Spinner/>
    {:else if past.length === 0}
      <div class="text-stone-500 text-center py-8">{t.general.noItems}</div>
    {:else}
      <div class="flex items-end gap-1 min-h-48 h-48">
        {#each past as it}
          {@const pts = it.acceptedPoints ?? 0}
          {@const heightPct = (pts / maxPoints) * 100}
          <div class="flex flex-col items-center flex-1 min-w-4 max-w-8 h-full justify-end" title="#{it.number} {formatDate(it.endDate)}: {pts} pts">
            <span class="text-xs text-stone-600 mb-1">{pts}</span>
            <div class="w-full rounded-t bg-blue-500 transition-all" style="height: {heightPct}%"></div>
            <span class="text-xs text-stone-500 mt-1 whitespace-nowrap">#{it.number}</span>
          </div>
        {/each}
      </div>
      <div class="mt-3 text-xs text-stone-500 text-center">{t.panels.velocity} — {t.iterations.points}</div>
    {/if}
  </div>
</Panel>
