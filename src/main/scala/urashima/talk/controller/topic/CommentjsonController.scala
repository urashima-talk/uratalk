package urashima.talk.controller.topic
import dispatch.json.JsValue
import java.util.logging.Logger
import java.util.Date
import javax.servlet.ServletRequest
import org.dotme.liquidtpl.controller.AbstractJsonDataController
import org.dotme.liquidtpl.{ Constants, LanguageUtil }
import urashima.talk.lib.util.AppConstants
import urashima.talk.service.TopicService
import urashima.talk.model.Comment

class CommentjsonController extends AbstractJsonDataController {

  Logger.getLogger(classOf[JsonController].getName)

  override def getList: JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.CommentProtocol._
    val startDate: Date = new Date
    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)

    val cursorNext: Option[String] = request.getParameter(Constants.KEY_CURSOR_NEXT) match {
      case null => None
      case v: String => Some(v)
      case _ => None
    }
    TopicService.fetchOne(topicId) match {
      case Some(topic) => {
        val resultSet = TopicService.getCommentResultList(topicId, cursorNext)
        if (resultSet.hasNext) {
          putExtraInformation(Constants.KEY_CURSOR_NEXT, resultSet.getEncodedCursor)
        }
        val list = resultSet.toArray.toList.map { obj =>
          obj.asInstanceOf[Comment]
        }
        tojson(list)
      }
      case None =>
        addError(Constants.KEY_GLOBAL_ERROR, LanguageUtil.get("error.dataNotFound"))
        tojson("")
    }
  }

  override def getDetail(id: String): JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.CommentProtocol._
    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
    TopicService.fetchOne(topicId) match {
      case Some(topic) => {
        TopicService.fetchComment(id, Some(topic)) match {
          case Some(comment) => {
            tojson(comment)
          }
          case None => {
            addError(Constants.KEY_GLOBAL_ERROR,
              LanguageUtil.get("error.dataNotFound"))
            tojson("")
          }
        }
      }
      case None => {
        addError(Constants.KEY_GLOBAL_ERROR,
          LanguageUtil.get("error.dataNotFound"))
        tojson("")
      }
    }

  }

  override def getForm(id: String): JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.CommentProtocol._
    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
    TopicService.fetchOne(topicId) match {
      case Some(topic) => {
        TopicService.fetchComment(id, Some(topic)) match {
          case Some(comment) => {
            tojson(comment)
          }
          case None => {
            val comment: Comment = TopicService.createNewComment(topic)
            val cookieUserName = TopicService.getCookieUserName(request)
            if ((cookieUserName != null) && (cookieUserName.size > 0)) {
              comment.setName(cookieUserName)
            }
            tojson(comment)
          }
        }
      }
      case None => {
        addError(Constants.KEY_GLOBAL_ERROR,
          LanguageUtil.get("error.dataNotFound"))
        tojson("")
      }
    }
  }
}

