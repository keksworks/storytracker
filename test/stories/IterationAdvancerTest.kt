package stories

import db.BaseMocks
import db.TestData
import db.TestData.date
import db.TestData.now
import db.TestData.story
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class IterationAdvancerTest: BaseMocks() {
  val advancer = create<IterationAdvancer>()
  val project = TestData.project.copy(currentIterationNum = 2, velocityAveragedOver = 2)
  val endDate: LocalDate = date
  val iteration1 = Iteration(project.id, 1, teamStrength = 100, startDate = endDate.minusWeeks(1), endDate = endDate, acceptedPoints = 6)
  val acceptedStory: Story = story.copy(acceptedAt = now, points = 4)

  private fun mockIterations(vararg its: Iteration) =
    every { iterationRepository.list(project.id, any(), any()) } returns its.toList()

  private fun mockAcceptedStories(vararg stories: Story) =
    every { storyRepository.list(*anyVararg()) } returns stories.toList()

  @Test fun `skips iteration when no accepted stories`() {
    mockIterations()
    mockAcceptedStories()
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    verify(exactly = 0) { iterationRepository.save(any()) }
  }

  @Test fun `saves iteration with correct accepted points`() {
    mockIterations()
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    verify { iterationRepository.save(match { it.number == 2 && it.acceptedPoints == 4 }) }
  }

  @Test fun `uses teamStrength from existing iteration record`() {
    val existingCurrent = Iteration(project.id, 2, teamStrength = 75, startDate = endDate.minusDays(5), endDate = endDate)
    mockIterations(existingCurrent)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    verify { iterationRepository.save(match { it.number == 2 && it.teamStrength == 75 }) }
  }

  @Test fun `uses startDate from existing iteration record`() {
    val customStart = endDate.minusDays(5)
    val existingCurrent = Iteration(project.id, 2, teamStrength = 100, startDate = customStart, endDate = endDate)
    mockIterations(existingCurrent)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    verify { iterationRepository.save(match { it.startDate == customStart }) }
  }

  @Test fun `uses previous iteration endDate as startDate when no existing record`() {
    mockIterations(iteration1)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    verify { iterationRepository.save(match { it.startDate == iteration1.endDate }) }
  }

  @Test fun `normalizes velocity by team strength`() {
    val weakIteration = iteration1.copy(teamStrength = 50, acceptedPoints = 6)
    mockIterations(weakIteration)
    mockAcceptedStories(acceptedStory.copy(points = 8))
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    // weakIteration: 6*100/50=12 normalized; new: 8*100/100=8; avg = (12+8)/2 = 10
    verify { projectRepository.save(match { it.velocity == 10 }) }
  }

  @Test fun `velocity with full team strength equals accepted points average`() {
    mockIterations(iteration1) // 6 pts, 100% team
    mockAcceptedStories(acceptedStory.copy(points = 4))
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    // (6 + 4) / 2 = 5
    verify { projectRepository.save(match { it.velocity == 5 }) }
  }

  @Test fun `advances currentIterationNum by 1`() {
    mockIterations(iteration1)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(this@IterationAdvancerTest.project, endDate)
    verify { projectRepository.save(match { it.currentIterationNum == project.currentIterationNum + 1 }) }
  }
}
