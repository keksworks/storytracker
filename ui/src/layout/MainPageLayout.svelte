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

<div class="min-h-screen flex flex-col">
  <Header {title}>
    <slot name="header"/>
  </Header>
  <div class="h-full px-2 sm:px-3 flex-grow bg-stone-100">
    <slot/>
  </div>
</div>
