<script lang="ts">
  import {navigate, t} from 'src/i18n'

  export let url = '/oauth/google'
  export let redirect = '/'
  export let label = ''
  export let narrow = false
</script>

<a href="{url}?redirect={redirect}" class="btn flex gap-2 !px-2.5 items-center outlined secondary {$$props.class ?? ''}">
  {label ? '' : t.login.link}
  <img src="/img/google.svg" class="h-4 w-4" title="Google Login" alt="">
  {#if label}
    {label}
  {:else}
    <button on:click|preventDefault={() => navigate('login#' + location.pathname)} title={t.login.email} class="border-l px-2 -my-2 -mr-2 flex items-center">
      <span class="text-lg mr-1">@</span> {narrow ? '' : t.contacts.email}
    </button>
  {/if}
</a>
