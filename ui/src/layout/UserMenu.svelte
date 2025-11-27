<script lang="ts">
  import {t} from 'src/i18n'
  import {logout, user} from 'src/stores/auth'
  import Icon from 'src/icons/Icon.svelte'
  import Dropdown from 'src/components/Dropdown.svelte'
  import Button from 'src/components/Button.svelte'
  import LoginButton from 'src/pages/login/LoginButton.svelte'
  import {isMobile} from 'src/pages/projects/context'

  let isDropdownOpen = false
</script>

{#if $user}
  <Dropdown class="right-0 !z-50" bind:open={isDropdownOpen}>
    <Button class="sm" color="primary" variant="soft">
      <div class="flex items-center gap-1 -mx-0.5">
        <Icon name="user" strokeWidth="1.5"/>
        <div>{isMobile ? $user.initials : $user.name}</div>
        <Icon name="chevron-down" class="opacity-50"/>
      </div>
    </Button>
    <svelte:fragment slot="open">
      <div class="py-0.5 px-1">
        <Button label={t.login.logout} variant="ghost" size="sm" icon="logout" on:click={logout}/>
      </div>
    </svelte:fragment>
  </Dropdown>
{:else}
  <LoginButton redirect={(location.pathname == '/' ? '/projects' : location.pathname)} class="default"/>
{/if}
