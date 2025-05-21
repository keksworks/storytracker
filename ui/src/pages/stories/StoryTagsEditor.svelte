<script lang="ts">
  import type {Project, Story} from 'src/api/types'
  import Badge from 'src/components/Badge.svelte'

  export let project: Project
  export let story: Story

  let focused = false

  function remove(i: number) {
    story.tags.splice(i, 1)
    story = story
  }

  function add(e: Event) {
    const input = e.target as HTMLInputElement
    const value = input.value.trim()
    if (value) {
      story.tags.push(value)
      story = story
      input.value = ''
    }
  }

  function keydown(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault()
      add(e)
    }
  }
</script>

<div class="border rounded bg-white flex flex-wrap items-center gap-1 p-1">
  {#each story.tags as tag, i}
    <Badge class="success whitespace-nowrap">{tag} <button class="ml-1 py-0.5" on:click={() => remove(i)}>✕</button></Badge>
  {/each}
  <input class="w-32 flex-1 border-none p-0 focus:outline-none focus:ring-0" on:change={add} on:keydown={keydown} on:focus={() => focused = true} on:blur={() => focused = false} list="tags-{story.id}">
  {#if focused}
    <datalist id="tags-{story.id}">
      {#each project.tags.filter(t => !story.tags.includes(t)) as tag}<option value={tag}>{/each}
    </datalist>
  {/if}
</div>
