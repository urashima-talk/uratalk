package urashima.talk.controller
import org.dotme.liquidtpl.controller.AbstractActionController
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

class SearchController extends AbstractActionController {

  override val logger = Logger.getLogger(classOf[SearchController].getName)

  override def getTemplateName: String = {
    "search"
  }

  override def getOuterTemplateName: String = {
    val buf: StringBuilder = new StringBuilder
    buf.append("outer")
      .append(File.separator)
      .append("dialog")
      .toString
  }

  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    val topicId = request.getParameter(AppConstants.KEY_TOPIC_ID)
    super.replacerMap + ("dialogTitle" -> { e => Text("%s".format(LanguageUtil.get("search"))) })
  }
}