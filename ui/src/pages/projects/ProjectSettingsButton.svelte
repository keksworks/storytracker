<script lang="ts">
  import Modal from 'src/components/Modal.svelte'
  import {t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import Form from 'src/forms/Form.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import TextAreaField from 'src/forms/TextAreaField.svelte'
  import NumberField from 'src/forms/NumberField.svelte'
  import SelectField from 'src/forms/SelectField.svelte'
  import GitHubWebhookDetails from './GitHubWebhookDetails.svelte'
  import api from 'src/api/api'
  import {showToast} from 'src/stores/toasts'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {navigate} from '@keksworks/svelte-tiny-router'

  export let project = {iterationWeeks: 1, defaultStoryPoints: 1, velocityAveragedOver: 3, isOwner: true} as ProjectContext
  export let label = t.projects.settings
  export let icon = "settings-filled"

  const isNew = !project.id
  export let show = false

  async function submit() {
    project = await api.post('projects' + (isNew ? '' : '/' + project.id), project)
    showToast(t.general.saved)
    show = false
    if (isNew) setTimeout(() => navigate(`/projects/${project.id}`), 500)
  }
</script>

<Button {icon} {label} on:click={() => show = true}/>
<Modal bind:show title={t.projects.project}>
  <Form {submit}>
    <FormField label={t.projects.name} bind:value={project.name} autofocus={isNew}/>
    <TextAreaField label={t.projects.description} bind:value={project.description} rows={3} required={false}/>
    <NumberField label={t.projects.defaultStoryPoints} bind:value={project.defaultStoryPoints} required={false} max={4}/>
    <NumberField label={t.projects.iterationWeeks} bind:value={project.iterationWeeks} required={false} min={1} max={4}/>
    <NumberField label={t.projects.velocityAveragedOver} bind:value={project.velocityAveragedOver} required={false} min={1} max={10}/>
    <SelectField label={t.projects.startDay} bind:value={project.startDay} options={t.weekDays}/>

    {#if project.id}
      <GitHubWebhookDetails {project}/>
    {/if}

    <Button type="submit" label={t.general.save} disabled={!project.isOwner}/>
  </Form>
</Modal>
