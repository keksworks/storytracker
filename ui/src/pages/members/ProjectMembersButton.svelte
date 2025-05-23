<script lang="ts">
  import type {Project, ProjectMemberUser} from 'src/api/types'
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import SortableTable from 'src/components/SortableTable.svelte'
  import ContactLink from 'src/components/ContactLink.svelte'
  import {formatDateTime} from '@codeborne/i18n-json'
  import InviteMemberButton from 'src/pages/members/InviteMemberButton.svelte'

  export let project: Project
  export let members: ProjectMemberUser[]

  export let show = false
</script>

<Button label={t.projects.members} on:click={() => show = true}/>

<Modal bind:show title={t.projects.members} wide>
  <SortableTable labels={t.users} columns={[
    ['name', m => m.user.name],
    ['initials', m => m.user.initials],
    ['email', m => m.user.email],
    ['role', m => m.member.role],
    ['lastLoginAt', m => m.user.lastLoginAt]
  ]} items={members} let:item={m}>
    <tr>
      <td>{m.user.name}</td>
      <td>{m.user.initials}</td>
      <td><ContactLink contact={m.user.email}/></td>
      <td>{t.users.roles[m.member.role]}</td>
      <td>{formatDateTime(m.user.lastLoginAt)}</td>
    </tr>
  </SortableTable>

  <InviteMemberButton {project} onadded={m => members = [...members, m]}/>
</Modal>
