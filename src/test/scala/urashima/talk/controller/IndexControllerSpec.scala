package urashima.talk.controller

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester
import org.dotme.liquidtpl.Constants

object IndexControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester(classOf[IndexController])
  Constants._pathPrefix = "war/"
  "IndexController" should {
    doBefore { tester.setUp; tester.start("/index") }

    "not null" >> {
      val controller = tester.getController[IndexController]
      controller mustNotBe null
    }
    "not redirect" >> {
      tester.isRedirect mustBe false
    }

    doAfter { tester.tearDown }

    "after tearDown" >> {
      true
    }
  }
}
class IndexControllerSpecTest extends JUnit4(IndexControllerSpec)
