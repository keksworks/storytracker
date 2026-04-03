<script lang="ts">
  import {formatDate, t} from 'src/i18n'

  export let iteration: {number?: number, points: number, startDate: string} | null
</script>

{#if iteration}
  <div class="bg-stone-300 px-3 py-2 flex justify-between border-t text-sm">
    <div>
      {#if iteration.number}
        <span class="font-medium mr-2" title={t.iterations.number}>{iteration.number}</span>
      {/if}
      <span title={t.iterations.startDate}>{formatDate(iteration.startDate)}</span>
    </div>
    <div class="font-bold" title={t.iterations.points}>{iteration.points}</div>
  </div>
{/if}
