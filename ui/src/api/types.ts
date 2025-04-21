// Generated automatically by ./gradlew types.ts
export type Id<T extends Entity<T>> = string & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}
export type TSID<T extends Entity<T>> = Id<T>

export type DayOfWeek = string

// class stories.Epic
export interface Epic {createdAt: Instant; createdBy?: TSID<User>; description?: string; id: TSID<Epic>; name: string; projectId: TSID<Project>; tag: string; updatedAt?: Instant}
// class stories.Project
export interface Project {bugsEstimatable: boolean; createdAt: Instant; description?: string; id: TSID<Project>; iterationWeeks: number; iterations: number; name: string; startDay: DayOfWeek; timezone: string; updatedAt?: Instant; velocityAveragedWeeks: number; version: number}
// class stories.ProjectMember$Role
export enum ProjectMemberRole {OWNER = 'OWNER', MEMBER = 'MEMBER', VIEWER = 'VIEWER'}
// class stories.ProjectMember
export interface ProjectMember {commentNotifications: boolean; createdAt: Instant; id: TSID<ProjectMember>; lastViewedAt?: Instant; mentionNotifications: boolean; projectId: TSID<Project>; role: ProjectMemberRole; updatedAt?: Instant; userId: TSID<User>}
// class stories.Story$Attachment
export interface StoryAttachment {filename: string; height?: number; size: number; thumbnailUrl?: URI; url: URI; width?: number}
// class stories.Story$Blocker
export interface StoryBlocker {createdAt: Instant; createdBy: TSID<User>; resolvedAt?: Instant; text?: string}
// class stories.Story$Comment
export interface StoryComment {attachments: Array<StoryAttachment>; createdAt: Instant; createdBy: TSID<User>; text?: string; updatedAt: Instant}
// class stories.Story$Status
export enum StoryStatus {ACCEPTED = 'ACCEPTED', DELIVERED = 'DELIVERED', FINISHED = 'FINISHED', STARTED = 'STARTED', REJECTED = 'REJECTED', PLANNED = 'PLANNED', UNSTARTED = 'UNSTARTED', UNSCHEDULED = 'UNSCHEDULED'}
// class stories.Story$Task
export interface StoryTask {completedAt?: Instant; createdAt: Instant; text: string}
// class stories.Story$Type
export enum StoryType {FEATURE = 'FEATURE', BUG = 'BUG', CHORE = 'CHORE', RELEASE = 'RELEASE'}
// class stories.Story
export interface Story {acceptedAt?: Instant; afterId?: TSID<Story>; blockers: Array<StoryBlocker>; comments: Array<StoryComment>; createdAt: Instant; createdBy?: TSID<User>; deadline?: LocalDate; description?: string; externalId?: string; id: TSID<Story>; name: string; points?: number; projectId: TSID<Project>; status: StoryStatus; tags: Array<string>; tasks: Array<StoryTask>; type: StoryType; updatedAt?: Instant}
// class users.ChangeLangRequest
export interface ChangeLangRequest {lang: string}
// class users.Role
export enum Role {OWNER = 'OWNER', ADMIN = 'ADMIN', VIEWER = 'VIEWER'}
// class users.User
export interface User {avatarUrl?: URI; createdAt: Instant; email: Email; firstName: string; id: TSID<User>; initials?: string; lang: string; lastName: string; lastOnlineAt?: Instant; name: string; role: Role; updatedAt?: Instant; username?: string}

// java.time.LocalDate
export type LocalDate = `${number}-${number}-${number}`
// java.time.Instant
export type Instant = `${number}-${number}-${number}T${number}:${number}:${number}Z`
// java.net.URI
export type URI = `${string}://${string}`
// klite.Email
export type Email = `${string}@${string}`

// db.TestData
export const date = "2025-03-03" as LocalDate
export const user = {"createdAt":"2025-04-21T10:27:35Z","email":"admin@artun.ee","firstName":"Test","id":"26vvzavo","lang":"en","lastName":"Admin","name":"Test Admin","role":"ADMIN","updatedAt":"2025-04-21T10:27:35Z"} as User
export const viewer = {"createdAt":"2025-04-21T10:27:35Z","email":"admin@artun.ee","firstName":"Test","id":"26vvzavp","lang":"en","lastName":"Viewer","name":"Test Viewer","role":"VIEWER","updatedAt":"2025-04-21T10:27:35Z"} as User
