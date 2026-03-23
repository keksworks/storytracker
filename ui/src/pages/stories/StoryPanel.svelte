<script lang="ts">
  import {type Story, StoryStatus} from 'src/api/types'
  import {formatDate, t} from 'src/i18n'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Panel from 'src/components/Panel.svelte'
  import StoryView from 'src/pages/stories/StoryView.svelte'
  import {autoscroll, stopAutoscroll} from 'src/shared/autoscroll'

  export let name: keyof typeof t.panels
  export let show: boolean | string | undefined
  export let project: ProjectContext
  export let stories: Story[] | undefined
  export let movable = project.canEdit
  export let velocity = 0
  export let status: StoryStatus | undefined = undefined
  export let onDrag: (detail: {id: number, status?: StoryStatus, beforeId?: number}) => void = () => {}
  export let onSearch: (tag: string) => void = () => {}
  export let onSaved: (story: Story) => void = () => {}
  export let onDelete: (story: Story) => void = () => {}
  export let onLocate: ((story: Story) => void) | undefined = undefined
  export let highlightStoryId: number | undefined = undefined

  let iterations: ({number?: number, points: number, startDate: string}|null)[] = []
  $: {
    iterations = []
    if (velocity && stories) {
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
    } else if (stories) {
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
  function onDrop(e: DragEvent) {
    onDrag({id: parseInt(e.dataTransfer?.getData('id')!), status})
    isDropTarget = false
  }
</script>

<Panel bind:show {name} class="stories">
  <slot slot="left" name="left"/>
  <slot slot="right" name="right"/>
  {#if stories}
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
      <StoryView {project} {story} {stories} {movable} {onSearch} {onDrag} {onSaved} {onDelete} {onLocate}
                  bind:highlightId={highlightStoryId}/>
    {/each}

    {#if movable}
      <div class="min-h-16" draggable={movable} on:drop={onDrop}
           on:dragover={e => {e.preventDefault(); isDropTarget = true; autoscroll(e)}}
           on:dragleave={() => {isDropTarget = false; stopAutoscroll()}} class:drop-target={isDropTarget}>
      </div>
    {/if}
  {:else}
    <Spinner/>
  {/if}
</Panel>

<style global>
  .panel h4 {
    text-transform: uppercase;
    font-size: 0.8rem;
    @apply py-1;
  }

  .stories.panel .drop-target {
    box-shadow: inset 0 1px 0 0 black;
  }
</style>
