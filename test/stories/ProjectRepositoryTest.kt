package stories

import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.TestData.project
import db.TestData.user
import org.junit.jupiter.api.Test
import users.UserRepository

class ProjectRepositoryTest: DBTest() {
  val repository = ProjectRepository(db)
  val memberRepository = ProjectMemberRepository(db)

  @Test fun `save & load`() {
    repository.save(project)
    expect(repository.get(project.id)).toEqual(project)
    expect(repository.list()).toContain(project)

    UserRepository(db).save(user)
    expect(repository.listForMember(user.id)).toBeEmpty()
    memberRepository.save(ProjectMember(project.id, user.id))
    expect(repository.listForMember(user.id)).toContain(project)
  }
}
