<script lang="ts">
  import type {ProjectContext} from 'src/pages/projects/context'
  import SortableTable from 'src/components/SortableTable.svelte'
  import {formatDateTime, t} from 'src/i18n'
  import ContactLink from 'src/components/ContactLink.svelte'
  import {type ProjectMemberRequest, type ProjectMemberUser, Role} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import Modal from 'src/components/Modal.svelte'
  import MemberForm from 'src/pages/projects/MemberForm.svelte'

  export let project: ProjectContext

  let editMember: ProjectMemberRequest|false = false

  function invite() {
    editMember = {role: Role.MEMBER} as ProjectMemberRequest
  }

  function edit(m: ProjectMemberUser) {
    editMember = {id: m.id, email: m.user.email, name: m.user.name, initials: m.user.initials, role: m.member.role} as ProjectMemberRequest
  }

  function onsaved(m: ProjectMemberUser) {
    project.members[m.user.id] = m
    editMember = false
  }
</script>

<SortableTable labels={t.users} columns={[
    ['name', m => m.user.name],
    ['initials', m => m.user.initials],
    ['email', m => m.user.email],
    ['role', m => m.member.role],
    ['lastLoginAt', m => m.user.lastLoginAt],
    ''
  ]} items={Object.values(project.members)} let:item={m}>
  <tr>
    <td>{m.user.name}</td>
    <td>{m.user.initials}</td>
    <td><ContactLink contact={m.user.email}/></td>
    <td>{t.users.roles[m.member.role]}</td>
    <td>{formatDateTime(m.user.lastLoginAt)}</td>
    <td>
      {#if project.isOwner}
        <Button label={t.general.edit} on:click={() => edit(m)}/>
      {/if}
    </td>
  </tr>
</SortableTable>

{#if project.isOwner}
  <Button label={t.projects.invite} on:click={invite} color="secondary"/>
{/if}

<Modal bind:show={editMember} title={editMember['id'] ? t.projects.member : t.projects.invite}>
  {#if editMember}
    <MemberForm {project} member={editMember} {onsaved}/>
  {/if}
</Modal>
