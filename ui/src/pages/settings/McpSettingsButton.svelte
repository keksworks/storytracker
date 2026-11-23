<script lang="ts">
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import api from 'src/api/api'
  import {showToast} from 'src/stores/toasts'
  import type {ApiKey} from 'src/api/types'

  export let show = false
  let key: ApiKey | undefined
  let loading = false

  $: if (show && !key) loadKey()

  async function loadKey() {
    loading = true
    const keys = await api.get<ApiKey[]>('api-keys')
    key = keys[0]
    if (!key) key = await api.post<ApiKey>('api-keys')
    loading = false
  }

  $: mcpUrl = key ? `${location.origin}/mcp` : ''
  $: mcpHeaders = key ? `Authorization: Bearer ${key.key}` : ''

  async function copy(value: string) {
    await navigator.clipboard.writeText(value)
    showToast(t.general.copied)
  }
</script>

<Button icon="settings-automation" label={t.settings.mcp} variant="ghost" size="sm" on:click={() => show = true}/>

<Modal bind:show title={t.settings.mcp}>
  {#if loading}
    <p class="text-sm text-gray-500">{t.general.loading}</p>
  {:else if key}
    <p class="text-sm text-gray-500 mb-4">{t.settings.mcpDescription}</p>
    <p class="text-xs text-muted mb-4">{t.settings.mcpCustomConnector}</p>

    <FormField label={t.settings.mcpUrl} value={mcpUrl} maxlength={1000} readonly>
      <Button slot="after" label={t.general.copy} on:click={() => copy(mcpUrl)}/>
    </FormField>

    <FormField label={t.settings.mcpHeaders} value={mcpHeaders} maxlength={1000} readonly>
      <Button slot="after" label={t.general.copy} on:click={() => copy(mcpHeaders)}/>
    </FormField>
  {/if}
</Modal>
