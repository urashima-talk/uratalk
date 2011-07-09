package urashima.talk.controller
import org.dotme.liquidtpl.controller.AbstractActionController
import org.dotme.liquidtpl.Constants
import scala.collection.mutable.MapBuilder
import scala.xml.{ Node, NodeSeq }
import urashima.talk.model.Topic
import urashima.talk.service.TopicService
import org.dotme.liquidtpl.LanguageUtil
import urashima.talk.controller.topic.BaseTopicListController

class FavoritesController extends AbstractActionController with BaseTopicListController {
  override def getTemplateName: String = {
    "favorites"
  }

  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    super.replacerMap + ("favoriteList" -> { e =>
      val list = TopicService.fetchAllFavorites(request)
      if ((list != null) && (list.size > 0)) {
        list.flatMap { topic =>
          TopicService.getTopicItemTemplate(topic)
        }
      } else {
        <li><div class="confirmMessage">{ LanguageUtil.get("error.dataNotFound") }</div></li>
      }

    },
      "topicItemTemplate" -> { e => TopicService.getTopicItemTemplate(null) })
  }
}