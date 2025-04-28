<script lang="ts">
  import MainPageLayout from 'src/layout/MainPageLayout.svelte'
  import {t} from 'src/i18n'
  import LangSelect from 'src/layout/LangSelect.svelte'
  import LoginCode from 'src/pages/login/LoginCode.svelte'
  import LoginButton from 'src/pages/login/LoginButton.svelte'

  let waitingForCode = false
</script>

<MainPageLayout title={t.login.link} width="narrow">
  <div slot="header">
    {#if !waitingForCode}<LangSelect/>{/if}
  </div>

  <LoginCode bind:waitingForCode>
    {#if !waitingForCode}
      <LoginButton label="Google Login" class="lg default w-full" redirect={location.hash.substring(1)}/>
    {/if}
  </LoginCode>
</MainPageLayout>
