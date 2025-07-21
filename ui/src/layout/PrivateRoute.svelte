<script lang="ts">
  import {Route} from 'svelte5-router'
  import PrivateRouteGuard from './PrivateRouteGuard.svelte'
  import type {SvelteComponentTyped} from 'svelte'

  export let path: string
  export let component: (new (args: { target: any; props?: any; }) => SvelteComponentTyped) | undefined = undefined
</script>

<Route {path}>
  {#snippet children(params)}
    <PrivateRouteGuard>
      <slot {params}>
        <svelte:component this={component} {...params} />
      </slot>
    </PrivateRouteGuard>
  {/snippet}
</Route>
