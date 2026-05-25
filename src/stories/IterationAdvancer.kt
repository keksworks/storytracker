package stories

import klite.error
import klite.info
import klite.jdbc.Transaction
import klite.jdbc.notNull
import klite.jobs.Job
import klite.logger
import klite.warn
import java.time.LocalDate
import java.time.temporal.ChronoUnit.WEEKS

class IterationAdvancer(
  private val projectRepository: ProjectRepository,
  private val iterationRepository: IterationRepository,
  private val storyRepository: StoryRepository,
): Job {
  private val log = logger()

  override suspend fun run() {
    advance(LocalDate.of(2026, 5, 26))
  }

  private fun advance(endDate: LocalDate) {
    var num = 0
    val dayOfWeek = endDate.dayOfWeek
    projectRepository.list(Project::startDay to dayOfWeek).forEach { p ->
      try {
        num++
        advanceFor(p, endDate)
        Transaction.current()!!.commit()
      } catch (e: Exception) {
        log.error(e)
        Transaction.current()!!.rollback()
      }
    }
    log.info("$num $dayOfWeek projects processed")
  }

  internal fun advanceFor(p: Project, endDate: LocalDate) {
    log.info("Advancing iteration of project ${p.id} ${p.name}")
    val num = p.currentIterationNum
    val lastIterations = iterationRepository.list(p.id, fromNumber = num - p.velocityAveragedOver + 1)
    val existingCurrentIteration = lastIterations.find { it.number == num }
    val prevIteration = lastIterations.find { it.number == num - 1 }
    val startDate = existingCurrentIteration?.startDate ?: prevIteration?.endDate ?: endDate.minusWeeks(p.iterationWeeks.toLong())
    if (startDate.plusWeeks(p.iterationWeeks.toLong()) > endDate)
      return log.warn("Skipping: iteration length mismatch for project ${p.id}, expected ${p.iterationWeeks} weeks but got ${WEEKS.between(startDate, endDate)}")
    val acceptedStories = storyRepository.list(Story::projectId to p.id, Story::acceptedAt to notNull, Story::iteration to null)
    if (acceptedStories.isEmpty()) return log.warn("Skipping non-active iteration")

    val iteration = Iteration(
      p.id, num, length = p.iterationWeeks,
      teamStrength = existingCurrentIteration?.teamStrength ?: 100,
      startDate = startDate, endDate = endDate,
      acceptedPoints = acceptedStories.sumOf { it.points ?: 0 },
    )
    iterationRepository.save(iteration)

    val allIterations = lastIterations.filter { it.number < num } + iteration
    val velocity = allIterations.sumOf { (it.acceptedPoints ?: 0) * 100 / maxOf(it.teamStrength, 1) } / allIterations.size
    projectRepository.save(p.copy(currentIterationNum = num + 1, velocity = velocity))
    storyRepository.setIteration(iteration, acceptedStories.map { it.id })
    log.info("Saved $iteration")

    Transaction.current()?.commit()
    storyRepository.reindexStoryOrder(p.id)
  }
}
