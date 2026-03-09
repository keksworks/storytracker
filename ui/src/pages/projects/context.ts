import type {Id, Project, ProjectMemberUser, User} from 'src/api/types'

export type ProjectContext = Project & {
  members: Record<Id<User>, ProjectMemberUser>
  epicTags: Set<string>
  isOwner: boolean
  canEdit: boolean
}

export const isMobile = innerWidth < 500
