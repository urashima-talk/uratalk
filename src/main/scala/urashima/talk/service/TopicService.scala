/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package urashima.talk.service

import com.google.appengine.api.datastore.{ Key, KeyFactory }
import com.google.appengine.api.taskqueue.TaskOptions._
import com.google.appengine.api.taskqueue.{ QueueFactory, TaskOptions }
import dispatch.json._
import java.util.logging.Logger
import java.util.Date
import org.dotme.liquidtpl.{ Constants, LanguageUtil }
import org.slim3.datastore.Datastore
import scala.collection.JavaConversions._
import sjson.json.JsonSerialization._
import sjson.json.{ DefaultProtocol, Format }
import urashima.talk.model.Topic
import sjson.json.JsonSerialization
import urashima.talk.model.Comment
import urashima.talk.meta.TopicMeta
import org.dotme.liquidtpl.lib.memcache.ReverseCounterLogService
import urashima.talk.meta.CommentMeta
import urashima.talk.lib.util.AppConstants

object TopicService {
  val logger = Logger.getLogger(TopicService.getClass.getName)

  object TopicProtocol extends DefaultProtocol {
    implicit object TopicFormat extends Format[Topic] {
      override def reads(json: JsValue): Topic = json match {
        case _ => throw new IllegalArgumentException
      }

      def writes(topic: Topic): JsValue = {
        JsObject(List(
          (JsString(Constants.KEY_ID), tojson(if (topic.getKey != null) KeyFactory.keyToString(topic.getKey) else null)),
          (JsString("name"), tojson(topic.getName)),
          (JsString("title"), tojson(topic.getTitle)),
          (JsString("content"), tojson(topic.getContent)),
          (JsString("isNoticed"), tojson(topic.isNoticed.toString)),
          (JsString("isHidden"), tojson(topic.isHidden.toString)),
          (JsString("createdAt"), if (topic.getCreatedAt != null) tojson(AppConstants.dateTimeFormat.format(topic.getCreatedAt)) else tojson(""))))
      }
    }
  }

  object CommentProtocol extends DefaultProtocol {
    import dispatch.json._
    import JsonSerialization._

    implicit object CommentFormat extends Format[Comment] {
      override def reads(json: JsValue): Comment = json match {
        case _ => throw new IllegalArgumentException
      }

      def writes(comment: Comment): JsValue = {
        val topic = comment.getTopicRef.getModel
        JsObject(List(
          (JsString(Constants.KEY_ID), tojson(if (comment.getKey != null) KeyFactory.keyToString(comment.getKey) else null)),
          (JsString(AppConstants.KEY_TOPIC_ID), tojson(if (comment.getTopicRef != null) KeyFactory.keyToString(comment.getTopicRef.getKey) else null)),
          (JsString("name"), tojson(topic.getName)),
          (JsString("title"), tojson(topic.getTitle)),
          (JsString("content"), tojson(topic.getContent)),
          (JsString("isNoticed"), tojson(topic.isNoticed.toString)),
          (JsString("isHidden"), tojson(topic.isHidden.toString)),
          (JsString("createdAt"), if (topic.getCreatedAt != null) tojson(AppConstants.dateTimeFormat.format(topic.getCreatedAt)) else tojson(""))))
      }
    }
  }

  def fetchOne(id: String): Option[Topic] = {
    val m: TopicMeta = TopicMeta.get
    try {
      val key = KeyFactory.stringToKey(id)
      Datastore.query(m).filter(m.key.equal(key)).asSingle match {
        case v: Topic => Some(v)
        case null => None
      }

    } catch {
      case e: Exception => {
        logger.severe(e.getMessage)
        logger.severe(e.getStackTraceString)
        None
      }
    }
  }

  def fetchAll(): List[Topic] = {
    val m: TopicMeta = TopicMeta.get
    Datastore.query(m).asList.toList
  }

  def createNew(): Topic = {
    val result: Topic = new Topic
    result.setName("")
    result.setTitle("")
    result.setContent("")
    result.setReferenceKey("")
    result.setHidden(false)
    result.setNoticed(false)
    result
  }

  def save(model: Topic): Key = {
    val key: Key = model.getKey
    if (key == null) {
      model.setKey(Datastore.createKey(classOf[Topic], ReverseCounterLogService.increment("t")))
    }

    val now: Date = new Date
    if (model.getCreatedAt == null) {
      model.setCreatedAt(now)
    }
    Datastore.put(model).apply(0)
  }

  def delete(topic: Topic) {
    QueueFactory.getQueue("default")
      .add(Builder.withUrl("/task/deletecomment")
        .param(Constants.KEY_ID, KeyFactory.keyToString(topic.getKey)))
    Datastore.delete(topic.getKey)
  }

  val isPublishedMapAll: List[(String, String)] = List[(String, String)](
    true.toString -> LanguageUtil.get("topic.isPublished.true"),
    false.toString -> LanguageUtil.get("topic.isPublished.false"));

  /**
   * Comment
   */
  def fetchCommentList(topicId: String): List[Comment] = {
    val m: CommentMeta = CommentMeta.get
    try {
      fetchOne(topicId) match {
        case Some(topic) => {
          Datastore.query(m).asList.toList
        }
        case None => null
      }
    } catch {
      case e: Exception => {
        logger.severe(e.getMessage)
        logger.severe(e.getStackTraceString)
        null
      }
    }
  }

  def fetchComment(id: String, _topic: Option[Topic]): Option[Comment] = {
    val m: CommentMeta = CommentMeta.get
    try {
      val key = KeyFactory.stringToKey(id)
      _topic match {
        case Some(topic) => {
          Datastore.query(m).filter(m.key.equal(key))
            .filter(m.topicRef.equal(topic.getKey)).asSingle match {
              case v: Comment => Some(v)
              case null => None
            }
        }
        case None => {
          Datastore.query(m).filter(m.key.equal(key)).asSingle match {
            case v: Comment => Some(v)
            case null => None
          }
        }
      }

    } catch {
      case e: Exception => {
        logger.severe(e.getMessage)
        logger.severe(e.getStackTraceString)
        None
      }
    }
  }

  def deleteComment(comment: Comment) {
    Datastore.delete(comment.getKey)
  }

  def createNewComment(): Comment = {
    val result: Comment = new Comment
    result.setName("")
    result.setTitle("")
    result.setContent("")
    result.setReferenceKey("")
    result.setHidden(false)
    result.setNoticed(false)
    result
  }

  def saveComment(model: Comment, topic: Topic): Key = {
    val key: Key = model.getKey
    val now: Date = new Date
    if (key == null) {
      model.setKey(Datastore.createKey(classOf[Comment], ReverseCounterLogService.increment("c")))
    }

    if (model.getCreatedAt == null) {
      model.setCreatedAt(now)
    }
    model.getTopicRef.setModel(topic)
    Datastore.put(model).apply(0)
  }

}
