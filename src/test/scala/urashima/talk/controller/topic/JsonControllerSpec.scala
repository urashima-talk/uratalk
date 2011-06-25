package urashima.talk.controller.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester

object JsonControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester( classOf[JsonController] )

  "JsonController" should {
    doBefore{ tester.setUp;tester.start("/topic/json")}

    "not null" >> {
      val controller = tester.getController[JsonController]
      controller mustNotBe null
    }
    "not redirect" >> {
      tester.isRedirect mustBe false
    }
    "get destination path is null" >> {
      tester.getDestinationPath mustBe null
    }

    doAfter{ tester.tearDown}

    "after tearDown" >> {
        true
    }
  }
}
class JsonControllerSpecTest extends JUnit4( JsonControllerSpec )
