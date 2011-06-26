package urashima.talk.controller
import org.dotme.liquidtpl.Constants
import org.dotme.liquidtpl.controller.AbstractActionController

class IndexController extends AbstractActionController {
  override def getTemplateName: String = {
    org.dotme.liquidtpl.Constants.ACTION_INDEX_TEMPLATE
  }
}