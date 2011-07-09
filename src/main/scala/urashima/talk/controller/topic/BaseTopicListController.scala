package urashima.talk.controller.topic
import org.dotme.liquidtpl.controller.AbstractActionController
import org.dotme.liquidtpl.LanguageUtil
import scala.collection.mutable.MapBuilder
import scala.xml.{Node, NodeSeq}

trait BaseTopicListController extends AbstractActionController {
  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    super.replacerMap + ("menuAdd" -> 
    { e =>  <a id="menuAdd" class="center" href="/topic/form" data-rel="dialog" data-transition="slidedown"><img id="menuAddIcon" src="/img/icon/add.png" alt={"%s".format(LanguageUtil.get("add"))} height="16" style="margin:0px;" /></a> })
  }
}