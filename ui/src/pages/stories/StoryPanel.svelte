<script lang="ts">
  import {type Iteration, type Story, StoryStatus} from 'src/api/types'
  import {t} from 'src/i18n'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Panel from 'src/components/Panel.svelte'
  import StoryView from 'src/pages/stories/StoryView.svelte'
  import IterationHeader from 'src/pages/stories/IterationHeader.svelte'
  import {stopAutoscroll} from 'src/shared/autoscroll'
  import {replaceValues} from '@codeborne/i18n-json'
  import Icon from 'src/icons/Icon.svelte'
  import {dragged} from 'src/shared/draggable'

  export let name: keyof typeof t.panels
  export let show: boolean
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
  export let flashStoryId: number | undefined = undefined
  export let collapseStory: ((story: Story) => boolean) | undefined = undefined
  export let iterationsData: Iteration[] = []
  export let onTeamStrengthSave: (iterationNum: number, ts: number) => Promise<void> = async () => {}

  let showCollapsed = false

  let activeStories: Story[] = []
  let collapsedStories: Story[] = []

  $: if (stories) {
    if (collapseStory) {
      collapsedStories = stories.filter(s => collapseStory(s))
      activeStories = stories.filter(s => !collapseStory(s))
    } else {
      activeStories = stories
    }
  }

  let iterations: ({number?: number, points: number, startDate: string, teamStrength?: number}|null)[] = []
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
      const itMap = new Map(iterationsData.map(it => [it.number, it]))
      let iteration: typeof iterations[number] | undefined
      for (const s of stories) {
        if (s.iteration! > (iteration?.number ?? 0)) {
          const itData = itMap.get(s.iteration!)
          iterations[s.id] = iteration = {number: s.iteration, points: 0, startDate: s.acceptedAt!, teamStrength: itData?.teamStrength}
        }
        if (iteration) iteration.points += s.points ?? 0
      }
    }
  }

  let isDropTarget = false
  function ondrop() {
    if (dragged.type === 'story' && dragged.id) onDrag({id: dragged.id, status})
    isDropTarget = false
  }
</script>

<Panel bind:show {name} class="stories">
  <slot slot="left" name="left"/>
  <slot slot="right" name="right"/>
  {#if !stories}
    <Spinner/>
  {:else}
    {#if collapsedStories.length}
      <div class="border-b border-stone-300">
        <button class="w-full px-3 py-2 flex items-center justify-between hover:bg-stone-100 transition-colors"
                onclick={() => showCollapsed = !showCollapsed}>
          <span class="text-sm font-medium text-stone-600">
            {replaceValues(t.stories.collapsed, {count: collapsedStories.length})}
          </span>
          <Icon name={showCollapsed ? 'chevron-up' : 'chevron-down'} class="cursor-pointer"/>
        </button>
        {#if showCollapsed}
          {#each collapsedStories as story (story.id)}
            {@const iteration = iterations[story.id]}
            <IterationHeader {iteration}
              canEdit={project.canEdit && (iteration?.number ?? 0) >= project.currentIterationNum - project.velocityAveragedOver}
              onTeamStrengthSave={iteration?.number !== undefined ? async (ts) => onTeamStrengthSave(iteration.number!, ts) : async () => {}}/>
            <StoryView {project} {story} {stories} movable={false} {onSearch} {onDrag} {onSaved} {onDelete} {onLocate}
                       bind:highlightId={highlightStoryId} bind:flashId={flashStoryId}/>
          {/each}
        {/if}
      </div>
    {/if}

    {#each activeStories as story, i (story.id ?? i)}
      {@const iteration = iterations[story.id]}
      <IterationHeader {iteration}
        canEdit={project.canEdit && (iteration?.number ?? 0) >= project.currentIterationNum - project.velocityAveragedOver}
        onTeamStrengthSave={iteration?.number !== undefined ? async (ts) => onTeamStrengthSave(iteration.number!, ts) : async () => {}}/>
      <StoryView {project} {story} {stories} {movable} {onSearch} {onDrag} {onSaved} {onDelete} {onLocate}
                  bind:highlightId={highlightStoryId} bind:flashId={flashStoryId}/>
    {/each}

    {#if movable}
      <div class="min-h-16 {isDropTarget ? 'drop-order' : ''}" draggable={movable} {ondrop}
           ondragover={e => {e.preventDefault(); isDropTarget = true}}
           ondragleave={() => {isDropTarget = false; stopAutoscroll()}}
           role="region" aria-label={t.stories.actions.move}>
      </div>
    {/if}
  {/if}
</Panel>

<style lang="postcss" global>
  .panel h4 {
    text-transform: uppercase;
    font-size: 0.8rem;
    @apply py-1;
  }

  .drop-order {
    box-shadow: inset 0 1px 0 0 black;
  }
</style>
