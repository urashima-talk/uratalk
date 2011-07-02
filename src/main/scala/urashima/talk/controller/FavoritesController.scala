package urashima.talk.controller
import org.dotme.liquidtpl.Constants
import org.dotme.liquidtpl.controller.AbstractActionController

class FavoritesController extends AbstractActionController {
  override def getTemplateName: String = {
    "favorites"
  }
}