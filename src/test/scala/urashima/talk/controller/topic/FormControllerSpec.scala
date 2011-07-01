package urashima.talk.controller.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester
import org.dotme.liquidtpl.Constants

object FormControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester(classOf[FormController])
  Constants._pathPrefix = "war/"
  "FormController" should {}
}
class FormControllerSpecTest extends JUnit4(FormControllerSpec)
