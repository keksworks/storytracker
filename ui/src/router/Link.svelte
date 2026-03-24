<script lang="ts">
  import {router} from 'src/router'

  export let to: string
  export let target: string|undefined = undefined
  export let label = ''
</script>

<a href={to} {target} use:router {...$$restProps} class="active:opacity-75 active:transition-opacity {$$props.class ?? 'text-link'}">
  <slot/>
  <span>{label}</span>
</a>
