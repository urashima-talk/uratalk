package urashima.talk.controller.task.topic

import org.slim3.controller.{ Controller, Navigation }
import urashima.talk.lib.util.AppConstants
import urashima.talk.meta.CommentMeta
import urashima.talk.model.Topic
import urashima.talk.service.TopicService
import org.slim3.datastore.Datastore
import urashima.talk.model.Comment
import urashima.talk.meta.TopicMeta
import scala.collection.JavaConversions._

class RefreshdateController extends Controller {

  @throws(classOf[Exception])
  override def run(): Navigation = {
    val topicId: String = request.getParameter(AppConstants.KEY_TOPIC_ID)
    if (topicId == null) {
      val m: TopicMeta = TopicMeta.get
      Datastore.query(m).asList.toList.foreach { topic =>
        {
          refresh(topic)
        }
      }
    } else {
      TopicService.fetchOne(topicId) match {
        case Some(topic) => {
          refresh(topic)
        }
        case None => response.getWriter.println("no topic data found")
      }
    }
    null;
  }

  def refresh(topic: Topic): Unit = {
    val m: CommentMeta = CommentMeta.get
    Datastore.query(m).filter(m.topicRef.equal(topic.getKey)).limit(1).asSingle match {
      case comment: Comment => {
        topic.setLastCommentNumberString(comment.getNumberString)
        topic.setLastCommentAt(comment.getCreatedAt)
        TopicService.save(topic)
      }
      case _ => {
        topic.setLastCommentNumberString("0")
        topic.setLastCommentAt(topic.getCreatedAt)
        TopicService.save(topic)
        response.getWriter.println("no comment data found")
      }
    }

  }
}
