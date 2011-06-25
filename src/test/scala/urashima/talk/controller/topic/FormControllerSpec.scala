package urashima.talk.controller.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester

object FormControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester( classOf[FormController] )

  "FormController" should {
    doBefore{ tester.setUp;tester.start("/topic/form")}

    "not null" >> {
      val controller = tester.getController[FormController]
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
class FormControllerSpecTest extends JUnit4( FormControllerSpec )
