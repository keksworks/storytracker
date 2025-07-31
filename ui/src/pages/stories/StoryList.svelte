<script lang="ts">
  import {type Project, type Story, StoryStatus} from 'src/api/types'
  import StoryView from 'src/pages/stories/StoryView.svelte'
  import {formatDate} from '@codeborne/i18n-json'
  import {createEventDispatcher} from 'svelte'
  import {t} from 'src/i18n'

  export let project: Project
  export let stories: Story[]
  export let status: StoryStatus | undefined = undefined
  export let movable = true
  export let velocity = 0

  let iterations: ({number?: number, points: number, startDate: string}|null)[] = []
  $: {
    iterations = []
    if (velocity) {
      let points = 0
      let date = new Date()
      for (const s of stories) {
        if (points + s.points! > velocity && s.status == StoryStatus.UNSTARTED) {
          date.setDate(date.getDate() + project.iterationWeeks * 7)
          iterations[s.id] = {points, startDate: date.toISOString()}
          points = 0
        } else {
          points += s.points ?? 0
        }
      }
    } else {
      // TODO: load real iterations from the server
      let iteration: typeof iterations[number] | undefined
      for (const s of stories) {
        if (s.iteration! > (iteration?.number ?? 0))
          iterations[s.id] = iteration = {number: s.iteration, points: 0, startDate: s.acceptedAt!}
        if (iteration) iteration.points += s.points ?? 0
      }
    }
  }

  let isDropTarget = false
  const dispatch = createEventDispatcher<{drag: {id: number, status?: StoryStatus}}>()
  function onDrop(e: DragEvent) {
    dispatch('drag', {id: parseInt(e.dataTransfer?.getData('id')!), status})
    isDropTarget = false
  }

  $: firstUnstarted = stories.find(s => s.status === status)
  $: firstUnaccepted = stories.find(s => s.status !== StoryStatus.ACCEPTED)
</script>

{#each stories as story, i (story.id ?? i)}
  {@const iteration = iterations[story.id]}
  {#if iteration}
    <div class="bg-stone-300 px-3 py-2 flex justify-between border-t">
      <div>
        {#if iteration.number}
          <span class="font-medium mr-2" title={t.iterations.number}>{iteration.number}</span>
        {/if}
        <span title={t.iterations.startDate}>{formatDate(iteration.startDate)}</span>
      </div>
      <div class="font-bold" title={t.iterations.points}>{iteration.points}</div>
    </div>
  {/if}
  <StoryView {project} {story} {movable} {firstUnstarted} {firstUnaccepted} on:search on:drag on:saved on:delete/>
{/each}

{#if movable}
  <div class="min-h-16" draggable={movable} on:drop={onDrop}
       on:dragover|preventDefault={() => isDropTarget = true} on:dragleave={() => isDropTarget = false} class:drop-target={isDropTarget}>
  </div>
{/if}
