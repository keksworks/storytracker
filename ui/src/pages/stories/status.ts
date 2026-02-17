import {type Instant, type Story, StoryStatus} from 'src/api/types'

export function onStatusChanged(s: Story) {
  if (s.status === StoryStatus.ACCEPTED) s.acceptedAt ??= new Date().toISOString() as Instant
  else s.acceptedAt = undefined
  return s
}
