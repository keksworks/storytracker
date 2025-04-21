export type Id<T extends Entity<T>> = number & {_of?: T}
export type Entity<T extends Entity<T>> = {id: Id<T>}
export type TSID<T extends Entity<T>> = Id<T>

export type DayOfWeek = string
