package urashima.talk.controller.topic

import java.util.logging.Logger
import dispatch.json.JsValue
import org.dotme.liquidtpl.controller.AbstractJsonDataController
import java.util.Date
import urashima.talk.service.TopicService
import urashima.talk.lib.util.AppConstants
import org.dotme.liquidtpl.Constants
import org.dotme.liquidtpl.LanguageUtil

class JsonController extends AbstractJsonDataController {

  Logger.getLogger(classOf[JsonController].getName)

  override def getList: JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.TopicProtocol._
    val startDate: Date = new Date
    tojson(TopicService.fetchAll())
  }

  override def getDetail(id: String): JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.TopicProtocol._
    val startDate: Date = new Date
    TopicService.fetchOne(id) match {
      case Some(topic) => {
        tojson(topic)
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
    import urashima.talk.service.TopicService.TopicProtocol._
    if ((id != null) && (id.size > 0)) {
      TopicService.fetchOne(id) match {
        case Some(topic) => {
          tojson(topic)
        }
        case None => {
          addError(Constants.KEY_GLOBAL_ERROR,
            LanguageUtil.get("error.dataNotFound"))
          tojson("")
        }
      }
    } else {
      tojson(TopicService.createNew)
    }
  }
}
