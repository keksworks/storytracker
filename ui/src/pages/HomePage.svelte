<script lang="ts">
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import FormField from 'src/forms/FormField.svelte'
  import Form from 'src/forms/Form.svelte'
  import api from 'src/api/api'
  import Button from 'src/components/Button.svelte'
  import Badge from 'src/components/Badge.svelte'

  let email = ''
  async function sendEmail() {
    api.post('contacts')
  }
</script>

<MainPageLayout title={t.title}>
  <section class="grid gap-10">
    <h1>
      FlowTracker - a Pivotal Tracker replacement
      <Badge class="success text-lg shadow relative -top-4">Coming soon!</Badge>
    </h1>

    <h3>Pivotal Tracker unfortunately goes away, so we take this opportunity to build a similar, but a bit more modern and improved tool.</h3>

    <p>Interested? We will let you know when it's ready!</p>
    <Form submit={sendEmail} class="flex gap-4">
      <FormField type="email" placeholder="Email" bind:value={email}/>
      <Button class="primary" label={t.general.submit}/>
    </Form>


    <div class="grid gap-4">
      <h4>These are the features we care about the most</h4>
      <ul class="list-disc list-inside">
        <li>No stories lost/hidden - everyone sees the same stories</li>
        <li>Easy drag-based for prioritization</li>
        <li>Flow/kanban style - you just work on the next item in backlog</li>
        <li>Velocity/estimation dates are tracked automatically</li>
        <li>In our experience, no tool can match the visibility and simplicity of this approach</li>
        <li>Suitable for truly Agile teams</li>
      </ul>
    </div>
  </section>
</MainPageLayout>
