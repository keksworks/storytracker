<script lang="ts">
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import {showToast} from 'src/stores/toasts'
  import type {Project} from 'src/api/types'

  export let project: Project

  $: webhookUrl = `${location.origin}/api/projects/${project.id}/github`

  async function copyValue(value: string) {
    await navigator.clipboard.writeText(value)
    showToast(t.projects.webhookCopied)
  }
</script>

<FormField label={t.projects.webhookUrl} value={webhookUrl} readonly>
  <Button slot="after" label={t.general.copy} on:click={() => copyValue(webhookUrl)}/>
</FormField>

<FormField label={t.projects.webhookSecret} value={project.webhookSecret} readonly>
  <Button slot="after" label={t.general.copy} on:click={() => copyValue(project.webhookSecret)}/>
</FormField>

<p class="text-xs text-muted">{t.projects.webhookInstructions}</p>
