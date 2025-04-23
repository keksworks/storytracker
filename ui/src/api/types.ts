// Generated automatically by ./gradlew types.ts
export type Id<T extends Entity<T>> = number & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}
export type TSID<T extends Entity<T>> = Id<T>

export type DayOfWeek = string

// class db.Id
export type Id<T> = number
// class stories.Epic
export interface Epic {comments: Array<StoryComment>; createdAt: Instant; createdBy?: Id<User>; description?: string; id: Id<Epic>; name: string; projectId: Id<Project>; tag: string; updatedAt?: Instant}
// class stories.Project
export interface Project {bugsEstimatable: boolean; createdAt: Instant; description?: string; id: Id<Project>; iterationWeeks: number; iterations: number; name: string; startDay: DayOfWeek; timezone: string; updatedAt?: Instant; velocityAveragedWeeks: number; version: number}
// class stories.ProjectMember$Role
export enum ProjectMemberRole {OWNER = 'OWNER', MEMBER = 'MEMBER', VIEWER = 'VIEWER'}
// class stories.ProjectMember
export interface ProjectMember {commentNotifications: boolean; createdAt: Instant; id: Id<ProjectMember>; lastViewedAt?: Instant; mentionNotifications: boolean; projectId: Id<Project>; role: ProjectMemberRole; updatedAt?: Instant; userId: Id<User>}
// class stories.Story$Attachment
export interface StoryAttachment {filename: string; height?: number; id?: number; size: number; width?: number}
// class stories.Story$Blocker
export interface StoryBlocker {createdAt: Instant; createdBy: Id<User>; resolvedAt?: Instant; text?: string}
// class stories.Story$Comment
export interface StoryComment {attachments: Array<StoryAttachment>; createdAt: Instant; createdBy: Id<User>; text?: string; updatedAt: Instant}
// class stories.Story$Status
export enum StoryStatus {ACCEPTED = 'ACCEPTED', DELIVERED = 'DELIVERED', FINISHED = 'FINISHED', STARTED = 'STARTED', REJECTED = 'REJECTED', PLANNED = 'PLANNED', UNSTARTED = 'UNSTARTED', UNSCHEDULED = 'UNSCHEDULED'}
// class stories.Story$Task
export interface StoryTask {completedAt?: Instant; createdAt: Instant; text: string}
// class stories.Story$Type
export enum StoryType {FEATURE = 'FEATURE', BUG = 'BUG', CHORE = 'CHORE', RELEASE = 'RELEASE'}
// class stories.Story
export interface Story {acceptedAt?: Instant; afterId?: Id<Story>; blockers: Array<StoryBlocker>; comments: Array<StoryComment>; createdAt: Instant; createdBy?: Id<User>; deadline?: LocalDate; description?: string; externalId?: string; id: Id<Story>; name: string; points?: number; projectId: Id<Project>; status: StoryStatus; tags: Array<string>; tasks: Array<StoryTask>; type: StoryType; updatedAt?: Instant}
// class users.ChangeLangRequest
export interface ChangeLangRequest {lang: string}
// class users.Role
export enum Role {OWNER = 'OWNER', ADMIN = 'ADMIN', VIEWER = 'VIEWER'}
// class users.User
export interface User {avatarUrl?: URI; createdAt: Instant; email: Email; firstName: string; id: Id<User>; initials?: string; lang: string; lastName: string; lastOnlineAt?: Instant; name: string; role: Role; updatedAt?: Instant; username?: string}

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
export const user = {"createdAt":"2025-04-23T08:27:35Z","email":"pivotal@codeborne.com","firstName":"Test","id":"171717188820","lang":"en","lastName":"Admin","name":"Test Admin","role":"ADMIN","updatedAt":"2025-04-23T08:27:35Z"} as User
export const viewer = {"createdAt":"2025-04-23T08:27:35Z","email":"viewer@codeborne.com","firstName":"Test","id":"171717188821","lang":"en","lastName":"Viewer","name":"Test Viewer","role":"VIEWER","updatedAt":"2025-04-23T08:27:35Z"} as User
