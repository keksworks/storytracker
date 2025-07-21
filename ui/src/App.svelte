<script lang="ts">
  import {t} from 'src/i18n'
  import {user} from 'src/stores/auth'
  import {Route, Router} from 'svelte5-router'
  import Toasts from './components/Toasts.svelte'
  import NotFoundPage from './layout/NotFoundPage.svelte'
  import Spinner from 'src/components/Spinner.svelte'
  import ProjectPage from 'src/pages/projects/ProjectPage.svelte'
  import HomePage from 'src/pages/HomePage.svelte'
  import ProjectsPage from 'src/pages/projects/ProjectsPage.svelte'
  import LoginPage from 'src/pages/login/LoginPage.svelte'
</script>

<svelte:head>
  <title>{t.title}</title>
</svelte:head>

<Toasts/>

<Router>
  <div class="App min-h-screen flex flex-col">
    <Route path="/" component={HomePage}/>
    <Route path="/login" component={LoginPage}/>
    {#if $user}
      <Route path="/projects" component={ProjectsPage}/>
      <Route path="/projects/:id" component={ProjectPage}/>
    {/if}
    <Route path="/samples/*path">
      {#snippet children(params)}
        {#await import('src/samples/SamplesPage.svelte')}
          <Spinner/>
        {:then samples}
          <svelte:component this={samples.default} path={params.path}/>
        {/await}
      {/snippet}
    </Route>
    <Route component={NotFoundPage}/>
  </div>
</Router>
