package urashima.talk.controller.task.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester
import org.dotme.liquidtpl.Constants

object RefreshdateControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester( classOf[RefreshdateController] )
  Constants._pathPrefix = "war/"
  "RefreshdateController" should {}
}
class RefreshdateControllerSpecTest extends JUnit4( RefreshdateControllerSpec )
