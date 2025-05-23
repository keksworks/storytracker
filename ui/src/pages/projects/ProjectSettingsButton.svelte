<script lang="ts">
  import type {Project} from 'src/api/types'
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

  export let project: Project

  export let show = false

  async function submit() {
    await api.post('projects', project)
    showToast(t.general.saved)
    show = false
  }
</script>

<Button label={t.projects.settings} on:click={() => show = true}/>

<Modal bind:show title={t.projects.project}>
  <Form {submit}>
    <FormField label={t.projects.name} bind:value={project.name}/>
    <TextAreaField label={t.projects.description} bind:value={project.description} rows={3} required={false}/>
    <NumberField label={t.projects.defaultStoryPoints} bind:value={project.defaultStoryPoints} required={false} max={4}/>
    <NumberField label={t.projects.iterationWeeks} bind:value={project.iterationWeeks} required={false} min={1} max={4}/>
    <NumberField label={t.projects.velocityAveragedOver} bind:value={project.velocityAveragedOver} required={false} min={1} max={10}/>
    <SelectField label={t.projects.startDay} bind:value={project.startDay} options={t.weekDays}/>
    <Button type="submit" label={t.general.save}/>
  </Form>
</Modal>
