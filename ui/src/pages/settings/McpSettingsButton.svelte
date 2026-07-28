<script lang="ts">
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import api from 'src/api/api'
  import {showToast} from 'src/stores/toasts'
  import type {ApiKey} from 'src/api/types'

  export let show = false
  let key: ApiKey | undefined
  let loading = false

  $: if (show && !key) loadKey()

  async function loadKey() {
    loading = true
    const keys = await api.get<ApiKey[]>('users/api-keys')
    key = keys[0]
    if (!key) key = await api.post<ApiKey>('users/api-keys')
    loading = false
  }

  $: mcpUrl = key ? `${location.origin}/mcp` : ''
  $: mcpConfig = key ? JSON.stringify({
    mcpServers: {
      storytracker: {
        url: mcpUrl,
        headers: {Authorization: `Bearer ${key.key}`}
      }
    }
  }, null, 2) : ''

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

    <div class="mb-4">
      <label class="block text-sm font-medium text-gray-700 mb-1">{t.settings.mcpConfig}</label>
      <div class="relative">
        <pre class="bg-gray-50 border rounded-md p-3 text-xs overflow-x-auto whitespace-pre-wrap">{mcpConfig}</pre>
        <div class="absolute top-2 right-2">
          <Button label={t.general.copy} size="xs" on:click={() => copy(mcpConfig)}/>
        </div>
      </div>
    </div>

    <p class="text-xs text-gray-400">{t.settings.mcpNote}</p>
  {/if}
</Modal>
