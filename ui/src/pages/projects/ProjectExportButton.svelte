<script lang="ts">

  import {t} from 'src/i18n'
  import type {ProjectContext} from 'src/pages/projects/context'

  export let project: ProjectContext
</script>

<a href="/api/projects/{project.id}/export" class="btn secondary solid">{t.projects.export}</a>
