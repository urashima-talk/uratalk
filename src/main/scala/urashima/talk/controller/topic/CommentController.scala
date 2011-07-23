package urashima.talk.controller.topic

import java.util.logging.Logger
import javax.servlet.http.HttpServletResponse
import org.dotme.liquidtpl.controller.AbstractFormController
import org.dotme.liquidtpl.{ Constants, LanguageUtil }
import scala.collection.mutable.MapBuilder
import scala.xml._
import urashima.talk.lib.util.{ AppConstants, TextUtils }
import urashima.talk.model.{ Comment, Topic }
import urashima.talk.service.TopicService
import javax.servlet.http.HttpServletRequest
import org.dotme.liquidtpl.helper.BasicHelper
import urashima.talk.controller.TitleListController

class CommentController extends AbstractFormController with TitleListController {
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
    val cursorNext: Option[String] = request.getParameter(Constants.KEY_CURSOR_NEXT) match {
      case null => None
      case v: String => Some(v)
      case _ => None
    }
    val resultSet = TopicService.getCommentResultList(topicId, cursorNext)
    val list = resultSet.toArray.toList.map { obj =>
      obj.asInstanceOf[Comment]
    }

    val topicTitleString: String = TopicService.fetchOne(topicId) match {
      case Some(topic) => "%s | %s".format(topic.getTitle, LanguageUtil.get("title"))
      case None => LanguageUtil.get("title")
    }

    val isItem: Boolean = (list != null) && (list.size > 0)
    super.replacerMap + ("commentList" -> { e =>
      if (isItem) {
        <ul id={ "commentList_%s".format(topicId) } class="commentList" data-role="listview">{
          list.flatMap { comment =>
            TopicService.getCommentItemTemplate(comment)
          }
        }</ul>
      } else {
        <ul id={ "commentList_%s".format(topicId) } class="commentList" data-role="listview"><li style="display:none;">%nbsp;</li></ul>
      }
    },
      "commentFormContainer" -> { e => <div id={ "commentFormContainer_%s".format(topicId) } style={ "display:%s;".format(if (isItem) "none" else "block") }></div> },
      "commentItemTemplate" -> { e => TopicService.getCommentItemTemplate(null) }, "menuAdd" ->
      { e => <a class={ "center menuAdd_%s".format(topicId) } href="#"><img id="menuAddIcon" src="/img/icon/comment.png" alt={ "%s %s".format(LanguageUtil.get("topic.comment"), LanguageUtil.get("add")) } height="16" style="margin:0px;"/></a> },
      "title" -> { e => Text(topicTitleString) },
      "topicTitle" -> { e => <strong id={ "topicTitle_%s".format(topicId) }></strong> },
      "topicContent" -> { e => <div id={ "topicContent_%s".format(topicId) } class="topicContent mt5" style="display:none;"></div> },
      "topicJson" -> { e => TopicService.getTopicJson(topicId) },
      "nextCommentButton" -> { e =>
        if (resultSet.hasNext) {
          val newCursor = resultSet.getEncodedCursor
          val onclick = "$.nextCommentList('%s', '%s')".format(topicId, newCursor);
          <div class={ "nextCommentButtonContainer_%s".format(topicId) }>
            <button class={ "nextCommentButton_%s".format(topicId) } onclick={ onclick } ontouchend={ onclick }>{ LanguageUtil.get("nextCursor") }</button>
          </div>
        } else {
          Text("")
        }
      })
  }
}