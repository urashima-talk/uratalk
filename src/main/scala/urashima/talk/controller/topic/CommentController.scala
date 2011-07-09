package urashima.talk.controller.topic

import java.util.logging.Logger
import javax.servlet.http.HttpServletResponse
import org.dotme.liquidtpl.controller.AbstractFormController
import org.dotme.liquidtpl.{ Constants, LanguageUtil }
import scala.collection.mutable.MapBuilder
import scala.xml._
import urashima.talk.lib.util.{ AppConstants, TextUtils }
import urashima.talk.model.{ Comment, Topic }
import urashima.talk.service.{ CommentChannelService, TopicService }
import javax.servlet.http.HttpServletRequest

class CommentController extends AbstractFormController {
  override val logger = Logger.getLogger(classOf[FormController].getName)

  override def redirectUri: String = {
    "reload";
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

  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
    val list = TopicService.fetchCommentList(topicId)
    val isItem: Boolean = (list != null) && (list.size > 0)
    super.replacerMap + ("commentList" -> { e =>
      if (isItem) {
        <ul id={ "commentList_%s".format(topicId) } data-role="listview">{
          list.flatMap { comment =>
            TopicService.getCommentItemTemplate(comment)
          }
        }</ul>
      } else {
        Text("")
      }
    },
      "commentFormContainer" -> { e => <div id={ "commentFormContainer_%s".format(topicId) } style={"display:%s;".format(if(isItem) "none" else "block")}></div> },
      "commentItemTemplate" -> { e => TopicService.getCommentItemTemplate(null) }, "menuAdd" ->
      { e => <a class={ "center menuAdd_%s".format(topicId) } href="#" onclick={"$.toggleCommentForm('%s');return false;".format(topicId)}><img id="menuAddIcon" src="/img/icon/comment.png" alt={ "%s %s".format(LanguageUtil.get("topic.comment"), LanguageUtil.get("add")) } height="16" style="margin:0px;"/></a> },
      "subTitle" -> { e => <strong id={ "subTitle_%s".format(topicId) }></strong> },
      "topicJson" -> { e => TopicService.getTopicJson(topicId) })
  }
}