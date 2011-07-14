package urashima.talk.controller
import org.dotme.liquidtpl.controller.AbstractActionController
import scala.collection.mutable.MapBuilder
import scala.xml.{ Node, NodeSeq, Text }
import urashima.talk.controller.topic.BaseTopicListController
import org.dotme.liquidtpl.LanguageUtil

trait TitleListController extends AbstractActionController {
  override def replacerMap: Map[String, ((Node) => NodeSeq)] = {
    super.replacerMap + ("title" ->
      { e => Text(title) })
  }

  def title: String = LanguageUtil.get("title")
}