package urashima.talk.controller.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester
import org.dotme.liquidtpl.Constants

object CommentjsonControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester(classOf[CommentjsonController])
  Constants._pathPrefix = "war/"
  "CommentjsonController" should {
    doBefore { tester.setUp; tester.start("/topic/commentjson") }

    "not null" >> {
      val controller = tester.getController[CommentjsonController]
      controller mustNotBe null
    }
    "not redirect" >> {
      tester.isRedirect mustBe false
    }
    "get destination path is null" >> {
      tester.getDestinationPath mustBe null
    }

    doAfter { tester.tearDown }

    "after tearDown" >> {
      true
    }
  }
}
class CommentjsonControllerSpecTest extends JUnit4(CommentjsonControllerSpec)
