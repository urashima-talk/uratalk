package urashima.talk.controller.topic

import org.dotme.liquidtpl.controller.AbstractFormController
import java.util.logging.Logger
import urashima.talk.lib.util.AppConstants
import org.dotme.liquidtpl.LanguageUtil
import urashima.talk.service.TopicService
import org.dotme.liquidtpl.Constants
import urashima.talk.model.Topic
import urashima.talk.model.Comment
import urashima.talk.lib.util.TextUtils
import urashima.talk.service.CommentChannelService
import javax.servlet.http.HttpServletResponse

class CommentController extends AbstractFormController {
  override val logger = Logger.getLogger(classOf[FormController].getName)

  override def redirectUri: String = {
    "/topic/comment?%s=%s".format(AppConstants.KEY_TOPIC_ID, request.getParameter(AppConstants.KEY_TOPIC_ID));
  }

  override def getTemplateName: String = {
    "comment"
  }

  override def validate: Boolean = {
    //Name
    val name = request.getParameter("name")
    if (name.size > 0 && name.size > AppConstants.VALIDATE_STRING_LENGTH) {
      addError("name", LanguageUtil.get("error.stringLength", Some(Array(
        LanguageUtil.get("topic.name"), "1", AppConstants.VALIDATE_STRING_LENGTH.toString))));
    }

    //Content
    val content = request.getParameter("content")
    if (content.size == 0) {
      addError("content", LanguageUtil.get("error.required", Some(Array(
        LanguageUtil.get("topic.content")))))
    } else if (content.size > AppConstants.VALIDATE_LONGTEXT_LENGTH) {
      addError("content", LanguageUtil.get("error.stringLength", Some(Array(
        LanguageUtil.get("topic.comment"), "1", AppConstants.VALIDATE_LONGTEXT_LENGTH.toString))));
    }

    !existsError
  }

  override def update: Boolean = {
    try {
      val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
      val id = request.getParameter(Constants.KEY_ID)
      TopicService.fetchOne(topicId) match {
        case Some(topic) => {
          val comment: Comment = if (id == null) {
            TopicService.createNewComment(topic)
          } else {
            TopicService.fetchComment(id, Some(topic)) match {
              case Some(v) => v
              case None => null
            }
          }

          if (comment != null) {
            //Name
            comment.setName(request.getParameter("name"))

            //Content
            comment.setContent(request.getParameter("content"))

            comment.setReferenceKey(TextUtils.encode(request.getRemoteAddr))

            TopicService.saveComment(comment, topic)
            response.asInstanceOf[HttpServletResponse].addCookie(TopicService.createCookieUserName(comment.getName))

            try {
              import sjson.json.JsonSerialization._
              import urashima.talk.service.TopicService.CommentProtocol._
              val channelId = CommentChannelService.getChannelId(request, topic.getNumberString);
              CommentChannelService.sendUpdateToUsers(topic.getNumberString, tojson(comment), channelId)
            } finally {
            }
          }
        }
        case None => null
      }
    } catch {
      case e: Exception => addError(Constants.KEY_GLOBAL_ERROR, LanguageUtil.get("error.systemError"));
    }
    !existsError
  }
}