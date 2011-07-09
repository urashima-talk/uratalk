package urashima.talk.controller.topic
import org.dotme.liquidtpl.controller.AbstractFormController
import java.util.logging.Logger
import urashima.talk.lib.util.AppConstants
import org.dotme.liquidtpl.LanguageUtil
import urashima.talk.service.TopicService
import org.dotme.liquidtpl.Constants
import urashima.talk.model.Topic
import urashima.talk.lib.util.TextUtils
import javax.servlet.http.HttpServletResponse
import com.google.appengine.api.datastore.KeyFactory
import java.io.File
import scala.xml._

class FormController extends AbstractFormController {
  var newId = ""

  override val logger = Logger.getLogger(classOf[FormController].getName)

  override def redirectUri: String = "/topic/comment?topicId=%s".format(newId);

  override def getTemplateName: String = {
    "form"
  }

  override def getOuterTemplateName: String = {
    val buf: StringBuilder = new StringBuilder
    buf.append("outer")
      .append(File.separator)
      .append("dialog")
      .toString
  }

  override def validate: Boolean = {
    //Name
    val name = request.getParameter("name")
    if (name.size > 0 && name.size > AppConstants.VALIDATE_STRING_LENGTH) {
      addError("name", LanguageUtil.get("error.stringLength", Some(Array(
        LanguageUtil.get("topic.name"), "1", AppConstants.VALIDATE_STRING_LENGTH.toString))));
    }

    //Title
    val title = request.getParameter("title")
    if (title.size <= 0 || title.size > AppConstants.VALIDATE_STRING_LENGTH) {
      addError("title", LanguageUtil.get("error.stringLength", Some(Array(
        LanguageUtil.get("topic.title"), "1", AppConstants.VALIDATE_STRING_LENGTH.toString))));
    }

    //Content
    val content = request.getParameter("content")
    if (content.size > AppConstants.VALIDATE_LONGTEXT_LENGTH) {
      addError("content", LanguageUtil.get("error.stringLength", Some(Array(
        LanguageUtil.get("topic.content"), "1", AppConstants.VALIDATE_LONGTEXT_LENGTH.toString))));
    }

    !existsError
  }

  override def update: Boolean = {

    try {
      val id = request.getParameter(Constants.KEY_ID)
      val topic: Topic = if (id == null) {
        TopicService.createNew
      } else {
        TopicService.fetchOne(id) match {
          case Some(_topic) => _topic
          case None => null
        }
      }

      if (topic != null) {
        //Name
        topic.setName(request.getParameter("name"))
        //Title
        topic.setTitle(request.getParameter("title"))
        //Content
        topic.setContent(request.getParameter("content"))

        topic.setReferenceKey(TextUtils.encode(request.getRemoteAddr))

        TopicService.save(topic)
        newId = KeyFactory.keyToString(topic.getKey)
        response.asInstanceOf[HttpServletResponse].addCookie(TopicService.createCookieUserName(topic.getName))
      }
    } catch {
      case e: Exception => addError(Constants.KEY_GLOBAL_ERROR, LanguageUtil.get("error.systemError"));
    }
    !existsError
  }

  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
    super.replacerMap + ("dialogTitle" -> { e => Text("%s %s".format(LanguageUtil.get("topic"), LanguageUtil.get("add"))) })
  }
}