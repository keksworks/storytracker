<script lang="ts">
  import {type Story, StoryStatus} from 'src/api/types'
  import StoryView from 'src/pages/stories/StoryView.svelte'
  import {formatDate} from '@codeborne/i18n-json'
  import {createEventDispatcher} from 'svelte'

  export let stories: Story[]
  export let status: StoryStatus | undefined = undefined
  export let movable = true
  export let velocity = 0

  let iterations: ({points: number, startDate: number}|null)[] = []
  $: if (velocity) {
    let points = 0
    iterations = []
    let date = new Date()
    for (const s of stories) {
      if (points + s.points! > velocity && s.status == StoryStatus.UNSTARTED) {
        date.setDate(date.getDate() + 7) // TODO: iteration length
        iterations.push({points, startDate: date.getTime()})
        points = 0
      } else {
        points += s.points ?? 0
        iterations.push(null)
      }
    }
  }

  let isDropTarget = false
  const dispatch = createEventDispatcher<{drag: {id: number, status?: StoryStatus}}>()
  function onDrop(e: DragEvent) {
    dispatch('drag', {id: parseInt(e.dataTransfer?.getData('id')!), status})
    isDropTarget = false
  }
</script>

{#each stories as story, i (story.id ?? i)}
  {@const iteration = iterations[i]}
  {#if iteration}
    <div class="bg-stone-300 px-3 py-2 flex justify-between border-t">
      <div class="font-medium">{formatDate(iteration.startDate)}</div>
      <div class="font-bold">{iteration.points}</div>
    </div>
  {/if}
  <StoryView {story} {movable} on:search on:drag on:saved on:delete/>
{/each}

{#if movable}
  <div class="min-h-16" draggable={movable} on:drop={onDrop}
       on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:drop-target={isDropTarget}>
  </div>
{/if}
