<script lang="ts">
  import type {Id, Project, Story} from 'src/api/types'
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import FormField from 'src/forms/FormField.svelte'
  import StoryPanel from 'src/pages/stories/StoryPanel.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let mode: 'input' | 'panel' = 'panel'
  export let project: ProjectContext
  export let stories: Story[] = []
  export let showDone = false
  export let initialOpenStoryId: number | undefined = undefined
  export let isMobile = false
  export let onSearch: ((q?: string) => void | Promise<void>) | undefined = undefined
  export let onSaved: (story: Story) => void = () => {}
  export let onDelete: (story: Story) => void = () => {}
  export let onLocate: (story: Story) => void = () => {}
  export let flashStoryId: number | undefined = undefined

  let searchQuery: string | undefined
  let showSearch = false
  let searchResults: Story[] | undefined
  let loadedSearchResults: Story[] | undefined

  export async function search(q?: string) {
    searchQuery = q
    showSearch = !!q
    loadedSearchResults = undefined
    if (q) {
      if (!showDone)
        loadedSearchResults = await api.get<Story[]>(`projects/${project.id}/stories?q=${encodeURIComponent(q)}&beforeIteration=${project.currentIterationNum}`)
    } else {
      searchQuery = undefined
    }
  }

  $: if (!showSearch) searchQuery = undefined

  function storyMatchesSearch(story: Story, q: string) {
    return story.id.toString() === q.replace(/^#/, '') ||
           story.name.toLowerCase().includes(q) ||
           story.description?.toLowerCase().includes(q) ||
           story.tags.some(t => t.toLowerCase().includes(q)) ||
           story.comments.some(c => c.text?.toLowerCase().includes(q))
  }

  async function triggerSearch(q?: string) {
    if (onSearch) await onSearch(q)
    else await search(q)
  }

  $: if (searchQuery?.length! > 2) {
    const q = searchQuery!.toLowerCase()
    searchResults = (loadedSearchResults ?? []).concat(stories.filter(s => storyMatchesSearch(s, q)))
  } else {
    searchResults = undefined
  }
</script>

{#if mode === 'input'}
  <FormField type="search" placeholder={t.stories.search.placeholder}
             on:keydown={e => e.key == 'Enter' && triggerSearch(e.currentTarget?.['value'])}
             on:input={e => isMobile && !e['value'] && triggerSearch()}
  />
{:else if mode === 'panel' && project}
  <StoryPanel name="search" bind:show={showSearch} {project} stories={searchResults} movable={false}
              onSearch={search} {onSaved} {onDelete} {onLocate} bind:flashStoryId
              collapseStory={s => s.iteration! < project.currentIterationNum && s.id !== initialOpenStoryId}>
    <span slot="right">{searchQuery}</span>
  </StoryPanel>
{/if}
