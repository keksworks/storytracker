<script lang="ts">
  import {StoryType} from 'src/api/types'
  import {t} from 'src/i18n'

  export let type: StoryType
</script>

<select bind:value={type} class="{$$props.class} border-0 bg-transparent cursor-pointer"
        style="background-image: none">
  {#each Object.values(StoryType) as typeValue}
    <option value={typeValue}>{t.stories.types[typeValue]}</option>
  {/each}
</select>
