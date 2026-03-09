<script lang="ts">
  import {type StoryComment} from 'src/api/types'
  import {t} from 'src/i18n'
  import {formatDateTime} from '@codeborne/i18n-json'
  import Button from 'src/components/Button.svelte'
  import {user} from 'src/stores/auth'

  export let comments: StoryComment[] | undefined
  export let urlBase: string
  export let canEdit: boolean

  function addComment() {
    comments = [...(comments || []), {
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      createdBy: $user.id,
      attachments: [],
    } as StoryComment]
  }
</script>

<h4>{t.stories.comments}</h4>
{#if comments}
  {#each comments as comment}
    <div>
      <div class="text-sm text-muted text-right py-2">{formatDateTime(comment.updatedAt)}</div>
      <div class="bg-white whitespace-pre-line p-2" bind:innerHTML={comment.text} contenteditable="true"></div>
      {#if comment.attachments}
        {#each comment.attachments as attachment}
          {@const url = `${urlBase}/attachments/${encodeURIComponent(attachment.filename)}`}
          <a href={url} target="_blank">
            {#if attachment.width && attachment.height}
              <img src={url} class="max-h-32 mt-2" alt={attachment.filename}>
            {:else}
              {attachment.filename}
            {/if}
          </a>
        {/each}
      {/if}
    </div>
  {/each}
{/if}
{#if canEdit}
  <Button variant="outlined" size="sm" class="mt-2" on:click={addComment}>＋</Button>
{/if}
