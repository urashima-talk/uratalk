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
import javax.servlet.http.{ Cookie, HttpServletRequest, HttpServletResponse }
import javax.servlet.{ ServletRequest, ServletResponse }
import org.dotme.liquidtpl.lib.memcache.{ CounterLogService, ReverseCounterLogService }
import org.dotme.liquidtpl.{ Constants, LanguageUtil }
import org.slim3.datastore.Datastore
import scala.collection.JavaConversions._
import sjson.json.JsonSerialization._
import sjson.json.{ DefaultProtocol, Format, JsonSerialization }
import urashima.talk.lib.util.AppConstants
import urashima.talk.meta.{ CommentMeta, TopicMeta }
import urashima.talk.model.{ Comment, Topic }

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
          (JsString("name"), tojson(if (topic.getName == null || topic.getName.size == 0) LanguageUtil.get("topic.defaultName") else topic.getName)),
          (JsString("title"), tojson(topic.getTitle)),
          (JsString("content"), tojson(topic.getContent)),
          (JsString("isNoticed"), tojson(topic.isNoticed.toString)),
          (JsString("isHidden"), tojson(topic.isHidden.toString)),
          (JsString("number"), tojson(topic.getNumberString)),
          (JsString("createdAt"), if (topic.getCreatedAt != null) tojson(AppConstants.dateTimeFormat.format(topic.getCreatedAt)) else tojson("")),
          (JsString("lastCommentAt"), if (topic.getLastCommentAt != null) tojson(AppConstants.dateTimeFormat.format(topic.getLastCommentAt)) else tojson("")),
          (JsString("lastCommentNumber"), tojson(topic.getLastCommentNumberString))))
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
          (JsString("name"), tojson(if (comment.getName == null || comment.getName.size == 0) LanguageUtil.get("topic.defaultName") else comment.getName)),
          (JsString("title"), tojson(comment.getTitle)),
          (JsString("content"), tojson(comment.getContent)),
          (JsString("isNoticed"), tojson(comment.isNoticed.toString)),
          (JsString("isHidden"), tojson(comment.isHidden.toString)),
          (JsString("number"), tojson(comment.getNumberString)),
          (JsString("createdAt"), if (comment.getCreatedAt != null) tojson(AppConstants.dateTimeFormat.format(comment.getCreatedAt)) else tojson(""))))
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
    Datastore.query(m).sort(m.lastCommentAt.desc).asList.toList
  }

  def fetchAllFavorites(request: ServletRequest): List[Topic] = {
    val cookies: Array[Cookie] = request.asInstanceOf[HttpServletRequest].getCookies
    if (cookies == null || cookies.size == 0) {
      List()
    } else {
      cookies.find { cookie: Cookie =>
        cookie.getName == AppConstants.KEY_COOKIE_FAVORITES
      } match {
        case Some(cookie) => {
          val m: TopicMeta = TopicMeta.get
          java.net.URLDecoder.decode(cookie.getValue(), Constants.CHARSET).split(",").map { id =>
            fetchOne(id).getOrElse(null)
          }.filter { v =>
            v != null
          }.sortWith { (x1, x2) =>
            x1.getLastCommentAt.after(x2.getLastCommentAt)
          }.toList
        }
        case None => {
          List()
        }
      }
    }
  }

  def createNew(): Topic = {
    val result: Topic = new Topic
    result.setName("")
    result.setTitle("")
    result.setContent("")
    result.setReferenceKey("")
    result.setHidden(false)
    result.setNoticed(false)
    result.setNumberString("")
    result.setLastCommentNumberString("")
    result
  }

  def save(model: Topic): Key = {
    val key: Key = model.getKey
    if (key == null) {
      model.setKey(Datastore.createKey(classOf[Topic], ReverseCounterLogService.increment("t")))
      model.setNumberString(CounterLogService.increment("t_all").toString)
    }

    val now: Date = new Date
    if (model.getCreatedAt == null) {
      model.setCreatedAt(now)
    }

    if (model.getLastCommentAt == null) {
      model.setLastCommentAt(now)
      model.setLastCommentNumberString("0")
    }

    Datastore.put(model).apply(0)
  }

  def delete(topic: Topic) {
    QueueFactory.getQueue("default")
      .add(Builder.withUrl("/task/deletecomment")
        .param(Constants.KEY_ID, KeyFactory.keyToString(topic.getKey)))
    Datastore.delete(topic.getKey)
  }

  /**
   * Comment
   */
  def fetchCommentList(topicId: String): List[Comment] = {
    val m: CommentMeta = CommentMeta.get
    try {
      fetchOne(topicId) match {
        case Some(topic) => {
          Datastore.query(m).filter(m.topicRef.equal(topic.getKey)).asList.toList
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

  def createNewComment(topic: Topic): Comment = {
    val result: Comment = new Comment
    result.setName("")
    result.setTitle("")
    result.setContent("")
    result.setReferenceKey("")
    result.setHidden(false)
    result.setNoticed(false)
    result.setNumberString("")
    result.getTopicRef.setModel(topic)
    result
  }

  def saveComment(model: Comment, topic: Topic): Key = {
    val key: Key = model.getKey
    val now: Date = new Date
    if (key == null) {
      model.setKey(Datastore.createKey(classOf[Comment], ReverseCounterLogService.increment("c")))
      model.setNumberString(CounterLogService.increment("c_%s".format(topic.getNumberString)).toString)
    }

    if (model.getCreatedAt == null) {
      model.setCreatedAt(now)
    }
    model.getTopicRef.setModel(topic)
    val result: Key = Datastore.put(model).apply(0)

    QueueFactory.getDefaultQueue.add(Builder.withUrl("/task/topic/refreshdate")
      .param(AppConstants.KEY_TOPIC_ID, KeyFactory.keyToString(topic.getKey))
      .method(Method.POST))
    result
  }

  def createCookieUserName(name: String): Cookie = {
    val newCookie: Cookie = new Cookie(AppConstants.KEY_COOKIE_USER_NAME, java.net.URLEncoder.encode(name, Constants.CHARSET))
    newCookie.setPath("/")
    newCookie
  }

  def getCookieUserName(request: ServletRequest): String = {
    val cookies: Array[Cookie] = request.asInstanceOf[HttpServletRequest].getCookies
    if (cookies == null || cookies.size == 0) {
      null
    } else {
      cookies.find { cookie: Cookie =>
        cookie.getName == AppConstants.KEY_COOKIE_USER_NAME
      } match {
        case Some(cookie) => {
          java.net.URLDecoder.decode(cookie.getValue, Constants.CHARSET)
        }
        case None => {
          null
        }
      }
    }
  }

}
