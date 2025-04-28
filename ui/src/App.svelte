<script lang="ts">
  import {t} from 'src/i18n'
  import {user} from 'src/stores/auth'
  import {Route, Router} from 'svelte-navigator'
  import Toasts from './components/Toasts.svelte'
  import NotFoundPage from './layout/NotFoundPage.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import ProjectPage from 'src/pages/ProjectPage.svelte'
  import HomePage from 'src/pages/HomePage.svelte'
  import ProjectsPage from 'src/pages/ProjectsPage.svelte'
</script>

<svelte:head>
  <title>{t.title}</title>
</svelte:head>

<Toasts/>

<Router primary={false}>
  <div class="App min-h-screen flex flex-col">
    <Route path="/" component={HomePage}/>
    {#if $user}
      <Route path="/projects" component={ProjectsPage}/>
      <Route path="/projects/:id" component={ProjectPage}/>
    {/if}
    <Route path="/samples/*path" let:params>
      {#await import('src/samples/SamplesPage.svelte')}
        <Spinner/>
      {:then samples}
        <svelte:component this={samples.default} path={params.path}/>
      {/await}
    </Route>
    <Route component={NotFoundPage}/>
  </div>
</Router>
