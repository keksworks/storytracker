<script lang="ts">
  import {replaceValues, t} from 'src/i18n'
  import api from 'src/api/api'
  import {initSession} from 'src/stores/auth'
  import Form from 'src/forms/Form.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import Button from 'src/components/Button.svelte'
  import {navigate} from '@keksworks/svelte-tiny-router'
  import type {User} from 'src/api/types'

  export let waitingForCode = false

  let email = '', code = ''

  async function submit() {
    if (waitingForCode) {
      const u = await api.post<User>('auth/email/code', {email, code})
      initSession(u)
      let to = location.hash.substring(1)
      if (to == '/') to = '/projects'
      navigate(to)
    } else {
      await api.post('auth/email', {email})
      waitingForCode = true
      code = ''
    }
  }
</script>

<Form {submit} class="w-full md:w-96 md:mt-10 mx-auto common-grid">
  {#if waitingForCode}
    <p>{replaceValues(t.login.codeSent, {email})}</p>
    <FormField type="tel" class="lg" label={t.login.code} bind:value={code} autofocus/>
    <Button type="submit" class="lg w-full" label={t.general.confirm}/>
    <Button variant="outlined" class="default lg w-full" label={t.general.cancel} onclick={() => waitingForCode = false}/>
  {:else}
    <FormField type="email" class="lg" label={t.contacts.email} bind:value={email} autofocus/>
    <Button type="submit" class="lg w-full" label={t.login.email}/>
  {/if}

  <slot/>
</Form>
