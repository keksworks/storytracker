<script lang="ts">

  import {t} from 'src/i18n'
  import type {ProjectContext} from 'src/pages/projects/context'
  import Icon from 'src/icons/Icon.svelte'

  export let project: ProjectContext
</script>

<a href="/api/projects/{project.id}/export" class="btn secondary solid"><Icon name = "link-external"/><span class ="ml-2">{t.projects.export}</span></a>
