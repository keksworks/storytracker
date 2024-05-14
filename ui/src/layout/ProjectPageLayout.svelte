<script lang="ts">
  import {t} from 'src/i18n'
  import Header from 'src/layout/Header.svelte'

  export let title = ''
</script>

<svelte:head>
  {#if title}
    <title>{t.title} - {title}</title>
  {/if}
</svelte:head>

<div class="h-screen overflow-hidden flex flex-col">
  <Header {title}/>
  <div class="flex flex-grow-0 overflow-hidden mx-auto px-2 sm:px-3 w-full">
    <slot/>
  </div>
</div>
