<script lang="ts">
  import Logo from 'src/layout/Logo.svelte'
  import LangSelect from 'src/layout/LangSelect.svelte'
  import Navigation from 'src/layout/Navigation.svelte'
  import UserMenu from 'src/layout/UserMenu.svelte'

  export let title = ''
</script>

<header class="py-4 flex justify-between items-center">
  {#if title}
    <h1 class="font-bold text-2xl mr-4">{title}</h1>
  {:else}
    <div class="sm:w-60"><Logo/></div>
  {/if}

  <div class="mr-auto">
    <Navigation/>
  </div>

  <div class="flex sm:gap-3 items-center">
    <LangSelect/>
    <UserMenu/>
  </div>
</header>
