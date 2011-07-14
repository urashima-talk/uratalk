package urashima.talk.controller
import org.dotme.liquidtpl.controller.AbstractActionController
import org.dotme.liquidtpl.Constants
import scala.collection.mutable.MapBuilder
import scala.xml._
import urashima.talk.model.Topic
import urashima.talk.service.TopicService
import org.dotme.liquidtpl.LanguageUtil
import urashima.talk.controller.topic.BaseTopicListController

class IndexController extends AbstractActionController with BaseTopicListController with TitleListController {
  override def getTemplateName: String = {
    org.dotme.liquidtpl.Constants.ACTION_INDEX_TEMPLATE
  }

  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    val cursorNext: Option[String] = request.getParameter(Constants.KEY_CURSOR_NEXT) match {
      case null => None
      case v: String => Some(v)
      case _ => None
    }
    val resultSet = TopicService.getResultList(cursorNext)
    super.replacerMap + ("topicList" -> { e =>
      val list = resultSet.toArray.toList
      if ((list != null) && (list.size > 0)) {
        list.flatMap { obj =>
          val topic: Topic = obj.asInstanceOf[Topic]
          TopicService.getTopicItemTemplate(topic)
        }
      } else {
        <li><div class="confirmMessage">{ LanguageUtil.get("error.dataNotFound") }</div></li>
      }

    },
      "topicItemTemplate" -> { e => TopicService.getTopicItemTemplate(null) },
      "nextTopicButton" -> { e =>
        if (resultSet.hasNext) {
          val newCursor = resultSet.getEncodedCursor
          val onclick = "$.nextTopicList('%s')".format(newCursor)
          <div class="nextTopicButtonContainer">
            <button class="nextTopicButton" onclick={ onclick } ontouchend={ onclick }>{ LanguageUtil.get("nextCursor") }</button>
          </div>
        } else {
          Text("")
        }
      })
  }

  def test = {
    <a id="menuAdd" class="center" href="/topic/form" data-rel="dialog" data-transition="slidedown"><img id="menuAddIcon" src="/img/icon/add.png" alt="スレッドをたてる" height="16" style="margin:0px;"/></a>
  }
}