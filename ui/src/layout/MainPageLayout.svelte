<script lang="ts">
  import {t} from 'src/i18n'
  import Container from 'src/layout/Container.svelte'
  import Header from 'src/layout/Header.svelte'

  export let title = ''
  export let width = 'normal'
</script>

<svelte:head>
  {#if title}
    <title>{t.title} - {title}</title>
  {/if}
</svelte:head>

<div class="min-h-screen relative">
  <div class="absolute inset-0 flex items-center justify-center -z-10"></div>

  <Container {width}>
    <Header {title}/>
    <slot/>
  </Container>
</div>
