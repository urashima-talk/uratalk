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
import urashima.talk.lib.util.TextUtils
import org.slim3.memcache.Memcache
import java.util.logging.Level
import scala.xml.{ NodeSeq, Text }
import org.dotme.liquidtpl.helper.BasicHelper

object TopicService {
  val logger = Logger.getLogger(TopicService.getClass.getName)

  val MC_KEY_COMMENT_COUNTER = "urashima.talk.service.TopicService#commentCounter_";

  val MC_KEY_TOPIC_LIST = "urashima.talk.service.TopicService#topicList";
  val MC_KEY_COMMENT_LIST = "urashima.talk.service.TopicService#commentList_";

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
    try {
      val cacheList = Memcache.get(MC_KEY_TOPIC_LIST).asInstanceOf[List[Topic]]
      if (cacheList == null) {
        throw new NullPointerException
      }
      cacheList
    } catch {
      case e =>
        val list = Datastore.query(m).sort(m.lastCommentAt.desc).asList.toList
        Memcache.put(MC_KEY_TOPIC_LIST, list)
        list
    }
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

    val result: Key = Datastore.put(model).apply(0)
    // delete list cache
    try {
      Memcache.delete(MC_KEY_COMMENT_LIST + model.getNumberString)
      Memcache.delete(MC_KEY_TOPIC_LIST)
    }
    return result
  }

  def delete(topic: Topic) {
    QueueFactory.getQueue("default")
      .add(Builder.withUrl("/task/deletecomment")
        .param(Constants.KEY_ID, KeyFactory.keyToString(topic.getKey)))

    val result: Unit = Datastore.delete(topic.getKey)

    // delete list cache
    try {
      Memcache.delete(MC_KEY_COMMENT_LIST + topic.getNumberString)
      Memcache.delete(MC_KEY_TOPIC_LIST)
    }
    result
  }

  /**
   * Comment
   */
  def fetchCommentList(topicId: String): List[Comment] = {
    val m: CommentMeta = CommentMeta.get
    try {
      fetchOne(topicId) match {
        case Some(topic) => {
          try {
            val cacheList = Memcache.get(MC_KEY_COMMENT_LIST + topic.getNumberString).asInstanceOf[List[Comment]]
            if (cacheList == null) {
              throw new NullPointerException
            }
            cacheList
          } catch {
            case e =>
              val list = Datastore.query(m).filter(m.topicRef.equal(topic.getKey)).asList.toList
              Memcache.put(MC_KEY_COMMENT_LIST + topic.getNumberString, list)
              list
          }
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
    val topic: Topic = comment.getTopicRef.getModel
    val result: Unit = Datastore.delete(comment.getKey)

    // refresh parent topic index
    QueueFactory.getDefaultQueue.add(Builder.withUrl("/task/topic/refreshdate")
      .param(AppConstants.KEY_TOPIC_ID, KeyFactory.keyToString(topic.getKey))
      .method(Method.POST))
      
    // delete list cache
    try {
      Memcache.delete(MC_KEY_COMMENT_LIST + topic.getNumberString)
    }
    result
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

      //commentNumber
      val topicNumber: String = topic.getNumberString
      val newNumber: Long = try {
        Memcache.increment(MC_KEY_COMMENT_COUNTER + topicNumber, 1).longValue;
      } catch {
        case e =>
          logger.log(Level.WARNING, "Failed to increment on Memcache: ", e);
          // if failed, restore the value last comment number
          try {
            topic.getLastCommentNumberString.toLong + 1
          } catch {
            case e => 1
          }
      }
      model.setNumberString(newNumber.toString)
    }

    if (model.getCreatedAt == null) {
      model.setCreatedAt(now)
    }
    model.getTopicRef.setModel(topic)
    val result: Key = Datastore.put(model).apply(0)

    // refresh parent topic index
    QueueFactory.getDefaultQueue.add(Builder.withUrl("/task/topic/refreshdate")
      .param(AppConstants.KEY_TOPIC_ID, KeyFactory.keyToString(topic.getKey))
      .method(Method.POST))

    // delete list cache
    try {
      Memcache.delete(MC_KEY_COMMENT_LIST + topic.getNumberString)
    }

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

  /*
   * NodeHelper
   */
  
  
  def getTopicItemTemplate(topic: Topic): NodeSeq = {
    val (topicId: String,
      title: String,
      lastCommentNumber: String,
      lastCommentAt: String) =
      try {
        (KeyFactory.keyToString(topic.getKey),
          topic.getTitle,
          topic.getLastCommentNumberString: String,
          AppConstants.dateTimeFormat.format(topic.getLastCommentAt))
      } catch {
        case e =>
          ("${id}",
            "${title}",
            "${lastCommentNumber}",
            "${lastCommentAt}")
      }
    <li>
      <a href={ "/topic/comment?topicId=%s".format(topicId) }>
        { "%s (%s)".format(title, lastCommentNumber) }
        <span class="alignright small">
          { lastCommentAt }
        </span>
      </a>
    </li>
  }
  
  def getTopicJson(topicId: String): NodeSeq =
    {
      import sjson.json.JsonSerialization._
      import urashima.talk.service.TopicService.TopicProtocol._
      try {
        val topic: Topic = TopicService.fetchOne(topicId) match {
          case Some(v) => v
          case None => TopicService.createNew
        } 
        BasicHelper.JsonTag("topic", tojson(topic))
      } catch {
        case e => Text("")
      }
    }

  def getCommentItemTemplate(comment: Comment): NodeSeq = {
    val (
      topicId: String,
      commentId: String,
      number: String,
      name: String,
      contentHtml: NodeSeq,
      createdAt: String) =
      try {
        (KeyFactory.keyToString(comment.getTopicRef.getKey),
          KeyFactory.keyToString(comment.getKey),
          comment.getNumberString,
          comment.getName,
          TextUtils.textToHtml(comment.getContent),
          AppConstants.dateTimeFormat.format(comment.getCreatedAt))
      } catch {
        case e =>
          ( 
            "${topicId}",
            "${id}",
            "${number}",
            "${name}",
            Text("{{html content}}"),
            "${createdAt}")
      }
    <li>
      <strong>{ number }.&nbsp;&nbsp;{ name }&nbsp;さん</strong>
      <span class="alignright small">
        <a class={"reply_%s-%s".format(number, topicId)}><img class="mr10" style="vertical-align: bottom;" height="16px" src="/img/icon/reply.png" onload={"$.addReplyListener('%s', '%s');".format(number, topicId)}/></a>{ createdAt }
      </span>
      <div class="mt5">
        { contentHtml }
      </div>
    </li>
  }


}
