package urashima.talk.controller.topic
import java.util.logging.Logger
import dispatch.json.JsValue
import org.dotme.liquidtpl.controller.AbstractJsonDataController
import java.util.Date
import urashima.talk.service.TopicService
import urashima.talk.lib.util.AppConstants
import org.dotme.liquidtpl.Constants
import org.dotme.liquidtpl.LanguageUtil

class CommentjsonController extends AbstractJsonDataController {

  Logger.getLogger(classOf[JsonController].getName)

  override def getList: JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.CommentProtocol._
    val startDate: Date = new Date

    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
    TopicService.fetchOne(topicId) match {
      case Some(topic) => {
        tojson(TopicService.fetchCommentList(request.getParameter(AppConstants.KEY_TOPIC_ID)))
      }
      case None =>
        addError(Constants.KEY_GLOBAL_ERROR, LanguageUtil.get("error.dataNotFound"))
        null
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
            null
          }
        }
      }
      case None => {
        addError(Constants.KEY_GLOBAL_ERROR,
          LanguageUtil.get("error.dataNotFound"))
        null
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
            addError(Constants.KEY_GLOBAL_ERROR,
              LanguageUtil.get("error.dataNotFound"))
            null
          }
        }
      }
      case None => {
        addError(Constants.KEY_GLOBAL_ERROR,
          LanguageUtil.get("error.dataNotFound"))
        null
      }
    }

  }
}

