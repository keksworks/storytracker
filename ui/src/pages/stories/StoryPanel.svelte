<script lang="ts">
  import type {Story, StoryStatus} from 'src/api/types'
  import type {t} from 'src/i18n'
  import StoryList from 'src/pages/stories/StoryList.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Panel from 'src/components/Panel.svelte'

  export let name: keyof typeof t.panels
  export let show: boolean | string | undefined
  export let project: ProjectContext
  export let stories: Story[] | undefined
  export let movable = project.canEdit
  export let velocity = 0
  export let status: StoryStatus | undefined = undefined
</script>

<Panel bind:show {name}>
  <slot slot="left" name="left"/>
  <slot slot="right" name="right"/>
  {#if stories}
    <StoryList {project} {stories} {status} {velocity} {movable} {...$$restProps}/>
  {:else}
    <Spinner/>
  {/if}
</Panel>
