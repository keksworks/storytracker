<script lang="ts">
  import {t} from 'src/i18n'
  import api from 'src/api/api'
  import type {Project, ProjectExport} from 'src/api/types'
  import Button from 'src/components/Button.svelte'
  import {navigate} from '@keksworks/svelte-tiny-router'
  import {showToast} from 'src/stores/toasts'
  import {replaceValues} from '@codeborne/i18n-json'

  export let projects: Project[]

  let fileInput: HTMLInputElement

   async function handleFileChange(event: Event) {
     const file = (event.target as HTMLInputElement).files?.[0]
     if (!file) return
     const text = await file.text()
     const data = JSON.parse(text) as ProjectExport

     const existing = projects.find(p => p.id == data.project.id)
     if (existing) {
       if (!confirm(replaceValues(t.projects.importExistsConfirm, existing))) {
         fileInput.value = ''
         return
       }
     }

     await api.post('projects/import', data)
     showToast(replaceValues(t.projects.importSuccess, data.project))
     navigate('/projects/' + data.project.id)
  }
</script>

<input type="file" accept=".json" hidden bind:this={fileInput} onchange={handleFileChange}/>
<Button icon="import" on:click={() => fileInput.click()}><span class="max-sm:hidden">{t.projects.import}</span></Button>
