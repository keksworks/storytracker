<script lang="ts">
  import Logo from 'src/layout/Logo.svelte'
  import LangSelect from 'src/layout/LangSelect.svelte'
  import Navigation from 'src/layout/Navigation.svelte'
  import UserMenu from 'src/layout/UserMenu.svelte'

  export let title = ''
</script>

<header class="bg-stone-50 border-b px-4 py-3 flex flex-wrap gap-3 justify-between items-center">
  <div class="flex gap-2.5 items-center">
    <Logo/>
    {#if title}
      <h1 class="font-bold text-2xl mr-4">{title}</h1>
    {/if}
  </div>

  <div class="mr-auto">
    <Navigation/>
  </div>

  <div class="flex flex-wrap gap-3 items-center">
    <slot/>
    <LangSelect/>
    <UserMenu/>
  </div>
</header>
