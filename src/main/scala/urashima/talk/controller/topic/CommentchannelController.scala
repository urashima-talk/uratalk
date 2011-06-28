package urashima.talk.controller.topic

import org.dotme.liquidtpl.controller.AbstractJsonController
import dispatch.json._
import urashima.talk.service.CommentChannelService

class CommentchannelController extends AbstractJsonController {
  override def getJson(): JsValue = {
    CommentChannelService.getTokenJson(request)
  }
}
