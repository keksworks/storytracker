<script lang="ts">
  import type {Story} from 'src/api/types'
  import Badge from 'src/components/Badge.svelte'

  export let story: Story

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

<div class="border rounded bg-white flex items-center gap-1" class:pl-1={story.tags.length}>
  {#each story.tags as tag, i}
    <Badge class="success">{tag} <button class="ml-1 py-0.5" on:click={() => remove(i)}>✕</button></Badge>
  {/each}
  <input class="flex-1 border-none rounded" on:blur={add} on:keydown={keydown}>
</div>
