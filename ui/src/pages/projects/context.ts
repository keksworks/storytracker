import type {Project, ProjectMemberUser} from 'src/api/types'

export type ProjectContext = Project & {
  members: ProjectMemberUser[]
  isOwner: boolean
  canEdit: boolean
}

export const isMobile = innerWidth < 500
