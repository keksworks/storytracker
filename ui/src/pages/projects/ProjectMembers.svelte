<script lang="ts">
  import type {ProjectContext} from 'src/pages/projects/context'
  import SortableTable from 'src/components/SortableTable.svelte'
  import {t} from 'src/i18n'
  import ContactLink from 'src/components/ContactLink.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import {type ProjectMemberRequest, type ProjectMemberUser, Role} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import Modal from 'src/components/Modal.svelte'
  import MemberForm from 'src/pages/projects/MemberForm.svelte'

  export let project: ProjectContext

  let edit: ProjectMemberRequest|false = false

  function invite() {
    edit = {role: Role.MEMBER} as ProjectMemberRequest
  }

  function onsaved(m: ProjectMemberUser) {
    edit = false
    project.members = [...project.members, m]
  }
</script>

<SortableTable labels={t.users} columns={[
    ['name', m => m.user.name],
    ['initials', m => m.user.initials],
    ['email', m => m.user.email],
    ['role', m => m.member.role],
    ['lastLoginAt', m => m.user.lastLoginAt]
  ]} items={project.members} let:item={m}>
  <tr>
    <td>{m.user.name}</td>
    <td>{m.user.initials}</td>
    <td><ContactLink contact={m.user.email}/></td>
    <td>{t.users.roles[m.member.role]}</td>
    <td>{formatDateTime(m.user.lastLoginAt)}</td>
  </tr>
</SortableTable>

<Button label={t.projects.invite} on:click={invite}/>

<Modal bind:show={edit} title={t.projects.invite}>
  {#if edit}
    <MemberForm {project} member={edit} {onsaved}/>
  {/if}
</Modal>
