<script lang="ts">
  import type {StoryComment} from 'src/api/types'
  import {formatDateTime, t} from 'src/i18n'
  import Button from 'src/components/Button.svelte'
  import {user} from 'src/stores/auth'
  import type {ProjectContext} from 'src/pages/projects/context'
  import {tick} from 'svelte'
  import {handleDescriptionClick, linkify} from 'src/shared/linkify'

  export let project: ProjectContext
  export let comments: StoryComment[] | undefined
  export let urlBase: string
  export let onSave: () => void = () => {}

  async function addComment() {
    comments = [...(comments || []), {
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      createdBy: $user.id,
      attachments: [],
    } as StoryComment]
    await tick()
    ;([...document.querySelectorAll('.comment')]?.last() as HTMLElement)?.focus()
  }

  function onDelete(i: number) {
    if (!confirm(t.stories.deleteCommentConfirm)) return
    comments!.splice(i, 1)
    comments = comments
  }

  function handleCommentKeyDown(e: KeyboardEvent) {
    handleDescriptionClick(e)
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) onSave()
  }
</script>

<h4>{t.stories.comments}</h4>
{#if comments}
  {#each comments as comment, i}
    <div>
      <div class="comment bg-white whitespace-pre-line p-2 min-h-8"
           bind:innerHTML={comment.text}
           contenteditable="true"
           onblur={() => comment.text = linkify(comment.text || '')}
           onclick={handleDescriptionClick}
           onkeydown={handleCommentKeyDown}
           role="textbox"
           tabindex="0"></div>
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
      <div class="flex justify-between items-center text-sm text-muted mb-1.5">
        <div>{project.members[comment.createdBy!]?.user?.name}</div>
        <div class="flex items-center">
          {formatDateTime(comment.updatedAt)}
          {#if project.canEdit}
            <Button icon="trash" title={t.stories.delete} variant="ghost" size="sm" on:click={() => onDelete(i)}/>
          {/if}
        </div>
      </div>
    </div>
  {/each}
{/if}

{#if project.canEdit}
  <Button variant="outlined" size="sm" on:click={addComment}>＋</Button>
{/if}
