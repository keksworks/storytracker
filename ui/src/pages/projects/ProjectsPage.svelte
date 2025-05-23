<script lang="ts">
  import type {Project} from 'src/api/types'
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'
  import Link from 'src/components/Link.svelte'
  import {onMount} from 'svelte'
  import ProjectSettingsButton from 'src/pages/projects/ProjectSettingsButton.svelte'

  let projects: Project[]

  onMount(async () => {
    projects = await api.get('projects')
  })
</script>

<MainPageLayout title={t.projects.title}>
  <span slot="header">
    <ProjectSettingsButton label={t.projects.new}/>
  </span>

  {#if !projects}
    <Spinner/>
  {/if}
  <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6 my-3 text-lg">
    {#each projects ?? [] as p}
      <Link to="projects/{p.id}" class="project border rounded-lg px-4 py-3 bg-white hover:bg-stone-50">
        <div class="font-semibold">{p.name}</div>
        <div class="text-muted">{t.projects.currentIterationNum} {p.currentIterationNum}</div>
      </Link>
    {/each}
  </div>
</MainPageLayout>
