<script lang="ts">
  import {pointsLabels} from 'src/pages/stories/points'

  export let points: number | undefined
</script>

<select bind:value={points} class="p-1 -m-1 border-0 bg-transparent text-blue-500 hover:text-blue-900 font-bold text-sm cursor-pointer"
        style="background-image: none" on:click|stopPropagation>
  {#each pointsLabels as label, i}
    <option value={i}>{label}</option>
  {/each}
</select>
