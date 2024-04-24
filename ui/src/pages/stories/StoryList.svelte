<script lang="ts">
  import type {Id, Story} from 'src/api/types'
  import StoryView from 'src/pages/stories/StoryView.svelte'
  import {formatDate} from '@codeborne/i18n-json'

  export let stories: Story[]
  export let velocity = 0

  let velocities: (number|null)[] = []
  $: if (velocity) {
    let v = 0
    velocities = []
    let date = new Date()
    for (const s of stories) {
      if (v + s.points > velocity) {
        v = 0
        date.setDate(date.getDate() + 7) // TODO: iteration length
        velocities.push(date.getTime())
      } else {
        v += s.points
        velocities.push(null)
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
  {#if velocities[i]}
    <div class="px-2 py-1">{formatDate(velocities[i])}</div>
  {/if}
  <StoryView bind:story on:drag={onDrag}/>
{/each}
