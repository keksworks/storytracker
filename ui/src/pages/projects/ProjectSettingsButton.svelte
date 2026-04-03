<script lang="ts">
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import Form from 'src/forms/Form.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import TextAreaField from 'src/forms/TextAreaField.svelte'
  import NumberField from 'src/forms/NumberField.svelte'
  import SelectField from 'src/forms/SelectField.svelte'
  import api from 'src/api/api'
  import {showToast} from 'src/stores/toasts'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {navigate} from '@keksworks/svelte-tiny-router'

  export let project = {iterationWeeks: 1, defaultStoryPoints: 1, velocityAveragedOver: 3, isOwner: true} as ProjectContext
  export let label = t.projects.settings

  const isNew = !project.id
  export let show = false

  $: webhookUrl = project.id ? `${location.origin}/api/projects/${project.id}/github` : ''

  async function submit() {
    project = await api.post('projects' + (isNew ? '' : '/' + project.id), project)
    showToast(t.general.saved)
    show = false
    if (isNew) setTimeout(() => navigate(`/projects/${project.id}`), 500)
  }

  async function copyValue(value: string) {
    await navigator.clipboard.writeText(value)
    showToast(t.projects.webhookCopied)
  }
</script>

<Button {label} on:click={() => show = true}/>

<Modal bind:show title={t.projects.project}>
  <Form {submit}>
    <FormField label={t.projects.name} bind:value={project.name} autofocus={isNew}/>
    <TextAreaField label={t.projects.description} bind:value={project.description} rows={3} required={false}/>
    <NumberField label={t.projects.defaultStoryPoints} bind:value={project.defaultStoryPoints} required={false} max={4}/>
    <NumberField label={t.projects.iterationWeeks} bind:value={project.iterationWeeks} required={false} min={1} max={4}/>
    <NumberField label={t.projects.velocityAveragedOver} bind:value={project.velocityAveragedOver} required={false} min={1} max={10}/>
    <SelectField label={t.projects.startDay} bind:value={project.startDay} options={t.weekDays}/>

    {#if webhookUrl}
      <div class="webhook-section">
        <label>{t.projects.webhookUrl}</label>
        <div class="webhook-field">
          <code>{webhookUrl}</code>
          <Button label={t.general.copy} on:click={() => copyValue(webhookUrl)}/>
        </div>
        <div class="webhook-field">
          <label>{t.projects.webhookSecret}</label>
          <div class="webhook-field">
            <code>{project.webhookSecret}</code>
            <Button label={t.general.copy} on:click={() => copyValue(project.webhookSecret)}/>
          </div>
        </div>
        <p class="webhook-hint">{t.projects.webhookInstructions}</p>
      </div>
    {/if}

    <Button type="submit" label={t.general.save} disabled={!project.isOwner}/>
  </Form>
</Modal>

<style>
  .webhook-section {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--border);
  }
  .webhook-section > label {
    display: block;
    font-weight: 500;
    margin-bottom: 0.25rem;
  }
  .webhook-field {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
  }
  .webhook-field code {
    flex: 1;
    font-size: 0.85rem;
    word-break: break-all;
    background: var(--bg-secondary);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
  }
  .webhook-hint {
    font-size: 0.8rem;
    color: var(--text-muted);
    margin-top: 0.5rem;
  }
</style>
