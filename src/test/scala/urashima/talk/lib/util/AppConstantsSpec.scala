package urashima.talk.lib.util

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.AppEngineTester

object AppConstantsSpec extends org.specs.Specification {
  val tester = new AppEngineTester

  "AppConstants" should {
    doBefore{ tester.setUp}

    "not null" >> {
      AppConstants mustNotBe null
    }

    doAfter{ tester.tearDown}

    "after tearDown" >> {
        true
    }
  }
}
class AppConstantsSpecTest extends JUnit4( AppConstantsSpec )
