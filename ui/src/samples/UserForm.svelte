<script lang="ts">
  import {replaceValues, t} from 'src/i18n'
  import type {User} from 'src/api/types'
  import Form from 'src/forms/Form.svelte'
  import Button from 'src/components/Button.svelte'
  import api from 'src/api/api'
  import {showToast} from 'src/stores/toasts'
  import FormField from 'src/forms/FormField.svelte'

  export let user: User = {} as User
  export let savePath = 'users'
  export let onSaved: (user: User) => void = () => {}

  async function submit() {
    user = await api.post(savePath, user)
    showToast(replaceValues(t.general.saved, user))
    onSaved(user)
  }
</script>

<Form {submit}>
  <div class="common-grid sm:grid-cols-2">
    <FormField label={t.users.name} bind:value={user.name} autofocus={!!user.id}/>
    <FormField label={t.contacts.email} type="email" bind:value={user.email}/>
  </div>

  <div class="flex justify-end">
    <Button type="submit" label={t.general.save} class="primary"/>
  </div>
</Form>
