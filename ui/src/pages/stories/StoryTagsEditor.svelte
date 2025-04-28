<script lang="ts">
  import FormField from 'src/forms/FormField.svelte'
  import type {Story} from 'src/api/types'

  export let story: Story
  let tags = story.tags.join(', ')
  $: story.tags = tags.split(',').map(t => t.trim()).filter(t => t)
</script>

<FormField bind:value={tags} on:keydown placeholder="Comma-separated for now"/>
