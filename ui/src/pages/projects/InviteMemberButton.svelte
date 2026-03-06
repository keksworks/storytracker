<script lang="ts">
  import {type Project, type ProjectMemberRequest, type ProjectMemberUser, Role} from 'src/api/types'
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import MemberForm from 'src/pages/projects/MemberForm.svelte'

  export let project: Project
  export let onsaved: (member: ProjectMemberUser) => void

  let show = false
  let member = {role: Role.MEMBER} as ProjectMemberRequest

  function saved(member: ProjectMemberUser) {
    show = false
    onsaved(member)
  }
</script>

<Button label={t.projects.invite} on:click={() => show = true}/>

<Modal bind:show title={t.projects.invite}>
  <MemberForm {project} {member} onsaved={saved}/>
</Modal>
