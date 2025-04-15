export type Id<T extends Entity<T>> = string & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}
export type TSID<T extends Entity<T>> = Id<T>

export interface Story {id: Id<Story>, title: string, description: string, tags: string[], points: number, createdAt: string, open?: boolean}
