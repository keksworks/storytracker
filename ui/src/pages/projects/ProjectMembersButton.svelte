<script lang="ts">
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import type {ProjectContext} from 'src/pages/projects/context'
  import ProjectMembers from 'src/pages/projects/ProjectMembers.svelte'

  export let project: ProjectContext

  let show = false
</script>

<Button icon="user" label={t.projects.members} hideLabelOnMobile on:click={() => show = true}/>

<Modal bind:show title={t.projects.members} wide>
  <ProjectMembers {project}/>
</Modal>
