<script>
  import {Role} from 'src/api/types'
  import {user} from 'src/stores/auth'
  import Link from 'src/components/Link.svelte'
  import {t} from 'src/i18n'

  const menu = [
    {path: '', label: t.home.title, roles: [Role.ADMIN, Role.CUSTOMER]},
    {path: 'project', label: t.projects.project, roles: [Role.ADMIN, Role.CUSTOMER]},
  ]
</script>

{#each menu.filter(m => m.roles.includes($user?.role)) as m}
  <Link to={m.path} label={m.label} class="btn sm mr-2 {location.pathname.substring(1).startsWith(m.path) ? 'bg-stone-100' : ''}"/>
{/each}

