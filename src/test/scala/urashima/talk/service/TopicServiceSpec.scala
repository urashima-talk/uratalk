package urashima.talk.service

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.AppEngineTester

object TopicServiceSpec extends org.specs.Specification {
  val tester = new AppEngineTester

  "TopicService" should {
    doBefore{ tester.setUp}

    "not null" >> {
      TopicService mustNotBe null
    }

    doAfter{ tester.tearDown}

    "after tearDown" >> {
        true
    }
  }
}
class TopicServiceSpecTest extends JUnit4( TopicServiceSpec )
