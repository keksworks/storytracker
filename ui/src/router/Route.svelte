<script lang="ts">
  import type {Component} from 'svelte'
  import {activePath, routeMatched} from './index'
  import Spinner from 'src/components/Spinner.svelte'

  type AnyComponent = Component<any, any, any>

  export let path = ''
  export let component: AnyComponent | Promise<{default: AnyComponent}> | undefined = undefined

  // Support for /:param and /*rest
  $: p = new RegExp('^' + path.replace(/(^|\/):([^\/]+)/g, '$1(?<$2>[^/]+)').replace(/(^|\/)\*([^\/]+)/g, '$1(?<$2>.*)') + '$')
  $: matched = $activePath.match(p)
  $: if (matched) $routeMatched = true
  $: params = matched?.groups
</script>

{#if matched || !path && !$routeMatched}
  {#if $$slots.default}
    <slot {...params}/>
  {:else if component instanceof Promise}
    {#await component}
      <Spinner/>
    {:then comp}
      <svelte:component this={comp.default} {...params}/>
    {/await}
  {:else}
    <svelte:component this={component} {...params}/>
  {/if}
{/if}
