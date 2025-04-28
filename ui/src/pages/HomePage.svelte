<script lang="ts">
  import {t} from 'src/i18n'
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
</script>

<MainPageLayout title={t.title}>
  <section class="grid gap-10 p-6 sm:p-12">
    <h1>
      StoryTracker - a Pivotal Tracker replacement
    </h1>

    <h3>Pivotal Tracker unfortunately goes away, so we take this opportunity to build a similar, but a bit more modern and improved tool.</h3>

    <div class="grid gap-4">
      <h4>These are the features we care about the most</h4>
      <ul class="list-disc list-inside">
        <li>No stories lost/hidden - everyone sees the same stories</li>
        <li>Easy drag-based prioritization</li>
        <li>Flow/kanban style - you just work on the next item in backlog</li>
        <li>Simple tag-based views</li>
        <li>Velocity/estimation dates are tracked automatically</li>
        <li>In our experience, no tool can match the visibility and simplicity of this approach</li>
        <li>Suitable for truly Agile teams</li>
      </ul>
    </div>
  </section>
</MainPageLayout>
