package stories

import db.today
import klite.error
import klite.info
import klite.jdbc.Transaction
import klite.jdbc.gte
import klite.jdbc.nowSec
import klite.jobs.Job
import klite.logger
import klite.warn
import java.time.LocalDate

class IterationAdvancer(
  private val projectRepository: ProjectRepository,
  private val iterationRepository: IterationRepository,
  private val storyRepository: StoryRepository,
): Job {
  private val log = logger()

  override suspend fun run() {
    advance(today)
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

  private fun advanceFor(p: Project, endDate: LocalDate) {
    // TODO: check for iteration length, if it is longer than 1 week
    log.info("Advancing iteration of project ${p.id} ${p.name}")
    val lastIterations = iterationRepository.list(p.id, fromNumber = p.currentIterationNum - p.velocityAveragedOver + 1)
    val startDate = lastIterations.find { it.number == p.currentIterationNum }?.endDate ?: endDate.minusWeeks(p.iterationWeeks.toLong())
    val acceptedStories = storyRepository.list(Story::projectId to p.id, Story::acceptedAt gte startDate, Story::iteration to null)
    // TODO: see if iteration already exists with overridden teamStrength
    val num = p.currentIterationNum
    val iteration = Iteration(
      p.id, num, length = 1,
      startDate = startDate, endDate = endDate,
      acceptedPoints = acceptedStories.sumOf { it.points ?: 0 },
    )
    if (acceptedStories.isEmpty())
      return log.warn("Skipping non-active iteration")

    iterationRepository.save(iteration)
    // TODO: take into account teamStrength
    val velocity = (lastIterations + iteration).sumOf { it.acceptedPoints ?: 0 } / (lastIterations.size + 1)
    projectRepository.save(p.copy(currentIterationNum = num + 1, velocity = velocity, updatedAt = nowSec()))
    storyRepository.setIteration(iteration, acceptedStories.map { it.id })
    log.info("Saved $iteration")
  }
}
