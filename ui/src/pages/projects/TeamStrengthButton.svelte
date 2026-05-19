<script lang="ts">
  import {t} from 'src/i18n'
  import Icon from 'src/icons/Icon.svelte'

  export let teamStrength: number = 100
  export let canEdit: boolean = true
  export let onSave: (teamStrength: number) => Promise<void> = async () => {}
  async function handleClick() {
    if (!canEdit) return
    const input = prompt(t.iterations.teamStrength + ' (0–100%)', teamStrength.toString())
    if (input === null) return
    const v = parseInt(input)
    if (!isNaN(v) && v >= 0 && v <= 100) {
      await onSave(v)
      teamStrength = v
    }
  }
</script>
<button on:click={handleClick} title={t.iterations.teamStrength}
        class="flex items-center gap-0.5 px-2 hover:bg-stone-200 rounded"
        class:cursor-default={!canEdit}>
  <Icon name="users" size="sm"/>
  <span class="text-sm">{teamStrength}%</span>
</button>
