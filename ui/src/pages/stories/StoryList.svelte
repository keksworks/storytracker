<script lang="ts">
  import type {Story} from 'src/api/types'

  export let stories: Story[]
</script>

{#each stories as s}
  <div class="story">{s.title}</div>
{/each}
