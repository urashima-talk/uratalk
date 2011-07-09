package urashima.talk.controller
import org.dotme.liquidtpl.controller.AbstractActionController
import org.dotme.liquidtpl.Constants
import scala.collection.mutable.MapBuilder
import scala.xml.{ Node, NodeSeq }
import urashima.talk.model.Topic
import urashima.talk.service.TopicService
import org.dotme.liquidtpl.LanguageUtil
import urashima.talk.controller.topic.BaseTopicListController

class IndexController extends AbstractActionController with BaseTopicListController {
  override def getTemplateName: String = {
    org.dotme.liquidtpl.Constants.ACTION_INDEX_TEMPLATE
  }

  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    super.replacerMap + ("topicList" -> { e =>
      val list = TopicService.fetchAll()
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
  
  def test = {
    <a id="menuAdd" class="center" href="/topic/form" data-rel="dialog" data-transition="slidedown"><img id="menuAddIcon" src="/img/icon/add.png" alt="スレッドをたてる" height="16" style="margin:0px;" /></a>
  }
}