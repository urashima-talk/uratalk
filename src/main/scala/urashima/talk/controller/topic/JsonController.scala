package urashima.talk.controller.topic

import java.util.logging.Logger
import dispatch.json.JsValue
import org.dotme.liquidtpl.controller.AbstractJsonDataController
import java.util.Date
import urashima.talk.service.TopicService
import urashima.talk.lib.util.AppConstants
import org.dotme.liquidtpl.Constants
import org.dotme.liquidtpl.LanguageUtil
import urashima.talk.model.Topic

class JsonController extends AbstractJsonDataController {

  Logger.getLogger(classOf[JsonController].getName)

  override def getList: JsValue = {
    import sjson.json.JsonSerialization._
    import urashima.talk.service.TopicService.TopicProtocol._
    
    val cursorNext: Option[String] = request.getParameter(Constants.KEY_CURSOR_NEXT) match {
      case null => None
      case v: String => Some(v)
      case _ => None
    }
    if (request.getParameter(AppConstants.MODE_FAVORITES) == "true") {
      tojson(TopicService.fetchAllFavorites(request))
    } else {
      val resultSet = TopicService.getResultList(cursorNext)
      if(resultSet.hasNext){
          putExtraInformation(Constants.KEY_CURSOR_NEXT, resultSet.getEncodedCursor)
      }
      val list = resultSet.toArray.toList.map { obj =>
        obj.asInstanceOf[Topic]
      }
      tojson(list)
    }
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
      val topic: Topic = TopicService.createNew
      val cookieUserName = TopicService.getCookieUserName(request)
      if ((cookieUserName != null) && (cookieUserName.size > 0)) {
        topic.setName(cookieUserName)
      }
      tojson(topic)
    }
  }
}
