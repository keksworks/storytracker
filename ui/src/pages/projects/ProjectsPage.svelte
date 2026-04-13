<script lang="ts">
  import type {Project} from 'src/api/types'
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'
  import {Link} from '@keksworks/svelte-tiny-router'
  import {onMount} from 'svelte'
  import ProjectSettingsButton from 'src/pages/projects/ProjectSettingsButton.svelte'
  import {formatDate} from '@codeborne/i18n-json'
  import FormField from 'src/forms/FormField.svelte'
  import ProjectImportButton from 'src/pages/projects/ProjectImportButton.svelte'

  let projects: Project[]
  let search = ''

  onMount(async () => {
    projects = await api.get('projects')
  })

  $: searchLowerCase = search.toLowerCase()
  $: filteredProjects = projects?.filter(p => p.name.toLowerCase().includes(searchLowerCase))

  function onKeyDown(e: KeyboardEvent) {
    if (e.key === 'Enter') (document.querySelector('a.project') as HTMLElement)?.click()
  }
</script>

<MainPageLayout title={t.projects.title}>
  <span slot="header" class="flex gap-4">
    <ProjectImportButton {projects}/>
    <ProjectSettingsButton icon="plus-hexagon" label={t.projects.new}/>
    <FormField type="search" placeholder={t.projects.search} bind:value={search} autofocus on:keydown={onKeyDown}/>
  </span>

  {#if !projects}
    <Spinner/>
  {/if}
  <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6 my-3 text-lg">
    {#each filteredProjects ?? [] as p}
      <Link to="/projects/{p.id}" class="project border rounded-lg px-4 py-3 bg-white hover:bg-stone-50">
        <div class="font-semibold">{p.name}</div>
        <div class="flex justify-between text-muted">
          <div>{t.projects.currentIterationNum} {p.currentIterationNum}</div>
          <div>{t.projects.velocity} {p.velocity}</div>
          <div title={t.projects.updatedAt}>{formatDate(p.updatedAt)}</div>
        </div>
      </Link>
    {/each}
  </div>
</MainPageLayout>
