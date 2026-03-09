<script lang="ts">
  import type {Story} from 'src/api/types'
  import Badge from 'src/components/Badge.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let project: ProjectContext
  export let story: Story

  let focused = false
  let value = ''

  function remove(i: number) {
    story.tags.splice(i, 1)
    story = story
  }

  function add() {
    value = value.trim()
    if (value) {
      story.tags.push(value)
      story = story
      if (!project.tags.includes(value)) project.tags.push(value)
      value = ''
    }
  }

  function keydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault()
      add()
    } else if (e.key === 'Backspace' && !value) {
      e.preventDefault()
      remove(story.tags.length - 1)
    }
  }
</script>

<div class="border rounded bg-white flex flex-wrap items-center gap-1 p-1">
  {#each story.tags as tag, i}
    <Badge class="whitespace-nowrap !py-0 {project.epicTags.has(tag) ? '!bg-purple-200' : 'success'}">
      {tag} <button class="ml-1 py-0.5" on:click={() => remove(i)}>✕</button>
    </Badge>
  {/each}
  <input class="w-32 flex-1 border-none py-0.5 pl-1 focus:outline-none focus:ring-0" bind:value on:change={add} on:keydown={keydown} on:focus={() => focused = true} on:blur={() => focused = false} list="tags-{story.id}">
  {#if focused}
    <datalist id="tags-{story.id}">
      {#each project.tags.filter(t => !story.tags.includes(t)) as tag}
        <option>{tag}</option>
      {/each}
    </datalist>
  {/if}
</div>
