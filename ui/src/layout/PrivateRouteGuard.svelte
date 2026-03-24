<script lang="ts">
  import {navigate} from 'src/router'
  import {user} from 'src/stores/auth'

  $: if (!$user) navigate('login', {replace: true})
</script>

{#if $user}
  <slot/>
{/if}
