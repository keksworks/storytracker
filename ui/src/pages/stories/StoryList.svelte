<script lang="ts">
  import type {Id, Story} from 'src/api/types'
  import StoryView from 'src/pages/stories/StoryView.svelte'
  import {formatDate} from '@codeborne/i18n-json'

  export let stories: Story[]
  export let velocity = 0

  let iterations: ({points: number, startDate: number}|null)[] = []
  $: if (velocity) {
    let points = 0
    iterations = []
    let date = new Date()
    for (const s of stories) {
      if (points + s.points! > velocity) {
        date.setDate(date.getDate() + 7) // TODO: iteration length
        iterations.push({points, startDate: date.getTime()})
        points = 0
      } else {
        points += s.points ?? 0
        iterations.push(null)
      }
    }
  }

  function onDrag(e: CustomEvent<{id: Id<Story>, beforeId: Id<Story>}>) {
    const fromIndex = stories.findIndex(s => s.id == e.detail.id)
    const story = stories.splice(fromIndex, 1)
    const toIndex = stories.findIndex(s => s.id == e.detail.beforeId)
    stories.splice(toIndex, 0, ...story)
    stories = stories
  }
</script>

{#each stories as story, i (story.id)}
  {@const iteration = iterations[i]}
  {#if iteration}
    <div class="bg-stone-200 px-3 py-2 flex justify-between border-t">
      <div class="font-medium">{formatDate(iteration.startDate)}</div>
      <div class="font-bold">{iteration.points}</div>
    </div>
  {/if}
  <StoryView bind:story on:drag={onDrag}/>
{/each}
