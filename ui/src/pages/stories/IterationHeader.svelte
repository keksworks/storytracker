<script lang="ts">
  import {formatDate, t} from 'src/i18n'
  import TeamStrengthButton from 'src/pages/projects/TeamStrengthButton.svelte'

  export let iteration: {number?: number, points: number, startDate: string, teamStrength?: number} | null
  export let canEdit = false
  export let onTeamStrengthSave: (ts: number) => Promise<void> = async () => {}
</script>

{#if iteration}
  <div class="bg-stone-300 px-3 py-2 flex justify-between border-t text-sm">
    <div>
      {#if iteration.number}
        <span class="font-medium mr-2" title={t.iterations.number}>{iteration.number}</span>
      {/if}
      <span title={t.iterations.startDate}>{formatDate(iteration.startDate)}</span>
    </div>
    <div class="flex items-center gap-1">
      {#if iteration.teamStrength !== undefined}
        <TeamStrengthButton teamStrength={iteration.teamStrength} {canEdit} onSave={onTeamStrengthSave}/>
      {/if}
      <div class="font-bold" title={t.iterations.points}>{iteration.points}</div>
    </div>
  </div>
{/if}
