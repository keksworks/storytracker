<script lang="ts">
  import {Role} from 'src/api/types'
  import {user} from 'src/stores/auth'
  import Link from 'src/components/Link.svelte'
  import {t} from 'src/i18n'

  const menu = [
    {path: '', label: t.home.title, roles: []},
    {path: 'projects', label: t.projects.title, roles: [Role.ADMIN, Role.OWNER, Role.VIEWER]}
  ]
</script>

{#if $user}
  {#each menu as m}
    <Link to={m.path} label={m.label} class="btn sm mr-2 {location.pathname.substring(1).startsWith(m.path) ? 'bg-stone-100' : ''}"/>
  {/each}
{/if}
