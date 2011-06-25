package urashima.talk.controller.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester

object CommentControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester( classOf[CommentController] )

  "CommentController" should {
    doBefore{ tester.setUp;tester.start("/topic/comment")}

    "not null" >> {
      val controller = tester.getController[CommentController]
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
class CommentControllerSpecTest extends JUnit4( CommentControllerSpec )
