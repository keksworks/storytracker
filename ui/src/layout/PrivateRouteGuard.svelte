<script lang="ts">
  import {navigate} from '@keksworks/svelte-tiny-router'
  import {user} from 'src/stores/auth'

  $: if (!$user) navigate('login', {replace: true})
</script>

{#if $user}
  <slot/>
{/if}
