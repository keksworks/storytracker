<script lang="ts">
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import type {Project} from 'src/api/types'
  import Button from 'src/components/Button.svelte'

  export let projects: Project[]

  let fileInput: HTMLInputElement

   async function handleFileChange(event: Event) {
     const file = (event.target as HTMLInputElement).files?.[0]
     if (!file) return
     const text = await file.text()
     const data = JSON.parse(text)

     const existing = projects.find(p => p.id == data.project.id)
     if (existing) {
       const confirmMessage = t.projects.updateConfirmation.replace('{0}', existing.name)
       if (!confirm(confirmMessage)) return
     }

     await api.post('projects/import', data)
     location.reload()
  }


</script>

<input type="file" accept=".json" hidden bind:this={fileInput} onchange={handleFileChange}/>
<Button label={t.projects.import} onclick={() => fileInput.click()}/>
