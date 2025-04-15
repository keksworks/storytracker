package db

import java.time.LocalDate

val today get() = LocalDate.now()
val yesterday get() = today.minusDays(1)
val tomorrow get() = today.plusDays(1)
