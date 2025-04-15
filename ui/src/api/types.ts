// Generated automatically by ./gradlew types.ts
export type Id<T extends Entity<T>> = string & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}
export type TSID<T extends Entity<T>> = Id<T>

export interface Story {id: Id<Story>, title: string, description: string, tags: string[], points: number, createdAt: string, open?: boolean}

// class stories.Project
export interface Project {bugsEstimatable: boolean; createdAt: Instant; description?: string; id: TSID<Project>; iterationWeeks: number; iterations: number; name: string; startDay: DayOfWeek; timezone: string; updatedAt?: Instant; velocityAveragedWeeks: number; version: number}
// class stories.Story$Attachment
export interface StoryAttachment {filename: string; height?: number; size: number; thumbnailUrl?: URI; url: URI; width?: number}
// class stories.Story$Comment
export interface StoryComment {attachments: Array<StoryAttachment>; createdAt: Instant; createdBy: TSID<User>; text?: string; updatedAt: Instant}
// class stories.Story$Status
export enum StoryStatus {ACCEPTED = 'ACCEPTED', DELIVERED = 'DELIVERED', FINISHED = 'FINISHED', STARTED = 'STARTED', REJECTED = 'REJECTED', PLANNED = 'PLANNED', UNSTARTED = 'UNSTARTED', UNSCHEDULED = 'UNSCHEDULED'}
// class stories.Story$Task
export interface StoryTask {completedAt?: Instant; createdAt: Instant; text: string}
// class stories.Story$Type
export enum StoryType {FEATURE = 'FEATURE', BUG = 'BUG', CHORE = 'CHORE', RELEASE = 'RELEASE'}
// class stories.Story
export interface Story {acceptedAt?: Instant; afterId?: TSID<Story>; blockerIds: Array<TSID<Story>>; comments: Array<StoryComment>; createdAt: Instant; createdBy?: TSID<User>; deadline?: LocalDate; description?: string; externalId?: string; id: TSID<Story>; name: string; points?: number; projectId: TSID<Project>; status: StoryStatus; tags: Array<string>; tasks: Array<StoryTask>; type: StoryType; updatedAt?: Instant}
// class users.ChangeLangRequest
export interface ChangeLangRequest {lang: string}
// class users.Role
export enum Role {ADMIN = 'ADMIN', VIEWER = 'VIEWER'}
// class users.User
export interface User {avatarUrl?: URI; email: Email; firstName: string; fullName: string; id: TSID<User>; lang: string; lastName: string; lastOnlineAt?: Instant; role: Role; updatedAt?: Instant}

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
export const user = {"email":"admin@artun.ee","firstName":"Test","fullName":"Test Admin","id":"26vvzavo","lang":"en","lastName":"Admin","role":"ADMIN"} as User
export const viewer = {"email":"admin@artun.ee","firstName":"Test","fullName":"Test Admin","id":"26vvzavp","lang":"en","lastName":"Admin","role":"VIEWER"} as User
