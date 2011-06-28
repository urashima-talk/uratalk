package urashima.talk.controller.topic

import org.specs.Specification
import org.specs.runner._
import org.slim3.tester.ControllerTester
import org.dotme.liquidtpl.Constants

object CommentchannelControllerSpec extends org.specs.Specification {

  val tester = new ControllerTester( classOf[CommentchannelController] )
  Constants._pathPrefix = "war/"
  "CommentchannelController" should {

  }
}
class CommentchannelControllerSpecTest extends JUnit4( CommentchannelControllerSpec )
