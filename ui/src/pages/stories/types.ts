import type {Story} from 'src/api/types'

export interface StoryHandlers {
  onSearch: (tag: string) => void
  onSaved: (story: Story) => void
  onDelete: (story: Story) => void
  onLocate?: (story: Story) => void
}

export interface StoryHighlight {
  storyId?: number
  flashId?: number
}

