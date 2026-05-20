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
  val iteration1 = Iteration(project.id, 1, teamStrength = 100, startDate = endDate.minusWeeks(2), endDate = endDate.minusWeeks(1), acceptedPoints = 6)
  val acceptedStory: Story = story.copy(acceptedAt = now, points = 4)

  private fun mockIterations(vararg its: Iteration) =
    every { iterationRepository.list(project.id, any(), any()) } returns its.toList()

  private fun mockAcceptedStories(vararg stories: Story) =
    every { storyRepository.list(*anyVararg()) } returns stories.toList()

  @Test fun `skips iteration when no accepted stories`() {
    mockIterations()
    mockAcceptedStories()
    advancer.advanceFor(project, endDate)
    verify(exactly = 0) { iterationRepository.save(any()) }
  }

  @Test fun `saves iteration with correct accepted points`() {
    mockIterations()
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(project, endDate)
    verify { iterationRepository.save(match { it.number == 2 && it.acceptedPoints == 4 }) }
  }

  @Test fun `uses teamStrength from existing iteration record`() {
    val existingCurrent = Iteration(project.id, 2, teamStrength = 75, startDate = endDate.minusWeeks(1), endDate = endDate)
    mockIterations(existingCurrent)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(project, endDate)
    verify { iterationRepository.save(match { it.number == 2 && it.teamStrength == 75 }) }
  }

  @Test fun `uses startDate from existing iteration record`() {
    val customStart = endDate.minusWeeks(1)
    val existingCurrent = Iteration(project.id, 2, teamStrength = 100, startDate = customStart, endDate = endDate)
    mockIterations(existingCurrent)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(project, endDate)
    verify { iterationRepository.save(match { it.startDate == customStart }) }
  }

  @Test fun `uses previous iteration endDate as startDate when no existing record`() {
    mockIterations(iteration1)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(project, endDate)
    verify { iterationRepository.save(match { it.startDate == iteration1.endDate }) }
  }

  @Test fun `normalizes velocity by team strength`() {
    val weakIteration = iteration1.copy(teamStrength = 50, acceptedPoints = 6)
    mockIterations(weakIteration)
    mockAcceptedStories(acceptedStory.copy(points = 8))
    advancer.advanceFor(project, endDate)
    // weakIteration: 6*100/50=12 normalized; new: 8*100/100=8; avg = (12+8)/2 = 10
    verify { projectRepository.save(match { it.velocity == 10 }) }
  }

  @Test fun `velocity with full team strength equals accepted points average`() {
    mockIterations(iteration1) // 6 pts, 100% team
    mockAcceptedStories(acceptedStory.copy(points = 4))
    advancer.advanceFor(project, endDate)
    // (6 + 4) / 2 = 5
    verify { projectRepository.save(match { it.velocity == 5 }) }
  }

  @Test fun `advances currentIterationNum by 1`() {
    mockIterations(iteration1)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(project, endDate)
    verify { projectRepository.save(match { it.currentIterationNum == project.currentIterationNum + 1 }) }
  }

  @Test fun `skips 2-week project when only 1 week has elapsed`() {
    val twoWeekProject = project.copy(iterationWeeks = 2)
    // prevIteration ended just 1 week before endDate → only 1 week elapsed, should skip
    val prevIteration = iteration1.copy(endDate = endDate.minusWeeks(1))
    mockIterations(prevIteration)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(twoWeekProject, endDate)
    verify(exactly = 0) { iterationRepository.save(any()) }
  }

  @Test fun `advances 2-week project when full 2 weeks have elapsed`() {
    val twoWeekProject = project.copy(iterationWeeks = 2)
    // prevIteration ended 2 weeks before endDate → exactly 2 weeks elapsed, should advance
    val prevIteration = iteration1.copy(endDate = endDate.minusWeeks(2))
    mockIterations(prevIteration)
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(twoWeekProject, endDate)
    verify { iterationRepository.save(match { it.number == 2 && it.length == 2 }) }
  }

  @Test fun `2-week project with no prior iteration uses iterationWeeks for startDate`() {
    val twoWeekProject = project.copy(iterationWeeks = 2)
    // no prior iterations → startDate = endDate - 2 weeks → length check passes
    mockIterations()
    mockAcceptedStories(acceptedStory)
    advancer.advanceFor(twoWeekProject, endDate)
    verify { iterationRepository.save(match { it.startDate == endDate.minusWeeks(2) }) }
  }

  @Test fun `velocity averages correctly across 2-week iterations`() {
    val twoWeekProject = project.copy(iterationWeeks = 2, velocityAveragedOver = 2)
    val prev = Iteration(twoWeekProject.id, 1, length = 2, teamStrength = 100,
      startDate = endDate.minusWeeks(4), endDate = endDate.minusWeeks(2), acceptedPoints = 8)
    mockIterations(prev)
    mockAcceptedStories(acceptedStory.copy(points = 4))
    advancer.advanceFor(twoWeekProject, endDate)
    // prev: 8*100/100=8 normalized; new: 4*100/100=4; avg=(8+4)/2=6
    verify { projectRepository.save(match { it.velocity == 6 }) }
  }
}
