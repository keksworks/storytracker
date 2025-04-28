<script lang="ts">
  import {navigate, replaceValues, t} from 'src/i18n'
  import api from 'src/api/api'
  import {initSession} from 'src/stores/auth'
  import Form from 'src/forms/Form.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import Button from 'src/components/Button.svelte'

  export let waitingForCode = false
  export let path = 'auth/email'
  export let codeSubmitted = (res: any) => {
    initSession(res)
    navigate(location.hash.substring(2) || 'projects')
  }

  let email = '', code = ''

  async function submit() {
    if (waitingForCode) {
      const res = await api.post(path + '/code', {email, code})
      codeSubmitted(res)
    } else {
      await api.post(path, {email})
      waitingForCode = true
    }
  }
</script>

<Form {submit} class="w-full md:w-96 md:mt-10 mx-auto common-grid">
  {#if waitingForCode}
    <p>{replaceValues(t.login.codeSent, {email})}</p>
    <FormField type="tel" class="lg" label={t.login.code} bind:value={code} autofocus/>
    <Button type="submit" class="primary lg w-full" label={t.general.confirm}/>
    <Button class="default lg w-full" label={t.general.cancel} onclick={() => waitingForCode = false}/>
  {:else}
    <FormField type="email" class="lg" label={t.contacts.email} bind:value={email} autofocus/>
    <Button type="submit" class="primary lg w-full" label={t.login.email}/>
  {/if}

  <slot/>
</Form>
