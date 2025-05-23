<script lang="ts">
  import {type Project, type ProjectMemberRequest, type ProjectMemberUser, Role} from 'src/api/types'
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import FormField from 'src/forms/FormField.svelte'
  import SelectField from 'src/forms/SelectField.svelte'
  import Button from 'src/components/Button.svelte'
  import Form from 'src/forms/Form.svelte'
  import api from 'src/api/api'
  import {showToast} from 'src/stores/toasts'

  export let project: Project
  export let onadded: (member: ProjectMemberUser) => void

  let show = false
  let member = {role: Role.MEMBER} as ProjectMemberRequest

  function prefill() {
    if (!member.name) {
      const names = member.email.substring(0, member.email.indexOf('@')).split('.').map(n => n[0].toUpperCase() + n.substring(1))
      member.name = names.join(' ')
    }
    if (!member.initials) {
      member.initials = member.name.split(' ').map(n => n[0].toUpperCase()).join('')
    }
  }

  async function submit() {
    onadded(await api.post<ProjectMemberUser>(`projects/${project.id}/members`, member))
    showToast(t.general.saved)
    show = false
  }
</script>

<Button label={t.projects.invite} on:click={() => show = true}/>

<Modal bind:show title={t.projects.invite}>
  <Form {submit}>
    <FormField label={t.users.email} bind:value={member.email} type="email" autofocus on:change={prefill}/>
    <FormField label={t.users.name} bind:value={member.name} on:change={prefill}/>
    <FormField label={t.users.initials} bind:value={member.initials}/>
    <SelectField label={t.users.role} bind:value={member.role} options={t.users.roles}/>
    <Button type="submit" label={t.general.save}/>
  </Form>
</Modal>
