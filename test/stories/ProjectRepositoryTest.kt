package stories

import ch.tutteli.atrium.api.fluent.en_GB.notToContain
import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toContain
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import db.DBTest
import db.TestData.admin
import db.TestData.project
import org.junit.jupiter.api.Test
import users.UserRepository

class ProjectRepositoryTest: DBTest() {
  val repository = ProjectRepository(db)
  val memberRepository = ProjectMemberRepository(db)

  @Test fun `save & load`() {
    repository.save(project)
    expect(repository.get(project.id)).toEqual(project)
    expect(repository.list()).toContain(project)

    UserRepository(db).save(admin)
    expect(repository.listForMember(admin.id)).toBeEmpty()
    memberRepository.save(ProjectMember(project.id, admin.id))
    expect(repository.listForMember(admin.id)).toContain(project)

    expect(repository.delete(project.id)).toEqual(1)
    expect(repository.list()).notToContain(project)
    expect(repository.listForMember(admin.id)).toBeEmpty()
  }
}
