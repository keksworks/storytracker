<script lang="ts">
  import {init} from './index'
  import {onMount} from 'svelte'

  onMount(init)
</script>

<slot/>
