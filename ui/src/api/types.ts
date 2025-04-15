// Generated automatically by ./gradlew types.ts
export type Id<T extends Entity<T>> = string & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}
export type TSID<T extends Entity<T>> = Id<T>

export interface Story {id: Id<Story>, title: string, description: string, tags: string[], points: number, createdAt: string, open?: boolean}

// class users.Address
export interface Address {apartment?: string; area?: string; building?: string; countryCode: string; county?: string; details?: string; id: string; municipality?: string; postalCode: string; street?: string}
// class users.ChangeLangRequest
export interface ChangeLangRequest {lang: string}
// class users.Role
export enum Role {ADMIN = 'ADMIN', VIEWER = 'VIEWER'}
// class users.User
export interface User {address?: Address; age?: number; avatarUrl?: URI; department?: string; email: Email; firstName: string; fullName: string; id: TSID<User>; lang: string; lastName: string; lastOnlineAt?: Instant; personalCode?: string; phone?: Phone; role: Role; updatedAt?: Instant}

// java.time.Instant
export type Instant = `${number}-${number}-${number}T${number}:${number}:${number}Z`
// java.net.URI
export type URI = `${string}://${string}`
// klite.Email
export type Email = `${string}@${string}`
// klite.Phone
export type Phone = `+${number}`

// db.TestData
export const address = {"building":"1/3","countryCode":"EE","county":"Harju maakond","details":"Karusambla tee 1/3, Rae vald","id":"3429052","municipality":"Rae vald","postalCode":"75304","street":"Karusambla tee"} as Address
export const date = "2025-03-03" as LocalDate
export const user = {"email":"admin@artun.ee","firstName":"Test","fullName":"Test Admin","id":"26vvzavo","lang":"et","lastName":"Admin","role":"ADMIN"} as User
export const viewer = {"email":"admin@artun.ee","firstName":"Test","fullName":"Test Admin","id":"26vvzavp","lang":"et","lastName":"Admin","role":"VIEWER"} as User
