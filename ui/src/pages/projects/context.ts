import type {Project, ProjectMemberUser} from 'src/api/types'

export type ProjectContext = Project & {
  members: ProjectMemberUser[]
}

export const isMobile = innerWidth < 500
