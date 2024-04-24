<script lang="ts">
  import {pointsLabels} from 'src/pages/stories/points'

  export let points: number
</script>

<select bind:value={points} class="p-1 -m-1 border-0 bg-transparent text-blue-500 font-bold text-sm" style="background-image: none">
  {#each pointsLabels as label, i}
    <option value={i}>{label}</option>
  {/each}
</select>
