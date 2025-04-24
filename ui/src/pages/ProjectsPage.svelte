<script lang="ts">
  import type {Project} from 'src/api/types'
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import api from 'src/api/api'
  import Spinner from 'src/components/Spinner.svelte'
  import Link from 'src/components/Link.svelte'

  let projects: Project[]
  async function load() {
    projects = await api.get('projects')
  }
  $: load()
</script>

<MainPageLayout title={t.projects.title}>
  {#if !projects}
    <Spinner/>
  {/if}
  <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-6 my-6 text-lg">
    {#each projects ?? [] as p}
      <Link to="projects/{p.id}" class="project border p-4 hover:bg-gray-200">
        {p.name}
        <div>{t.projects.currentIterationNum}: {p.currentIterationNum}</div>
      </Link>
    {/each}
  </div>
</MainPageLayout>
