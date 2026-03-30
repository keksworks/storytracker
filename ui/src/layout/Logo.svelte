<script lang="ts">
  import {t} from 'src/i18n'
  import {Link} from '@keksworks/svelte-tiny-router'
</script>

<Link to="/">
  <img class="h-7" src="/img/logo.png" title={t.title} alt="Logo">
</Link>
