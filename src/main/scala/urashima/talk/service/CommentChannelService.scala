/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package urashima.talk.service

import com.google.appengine.api.channel.{ ChannelMessage, ChannelService, ChannelServiceFactory }
import com.google.appengine.api.datastore.{ DatastoreServiceFactory, DatastoreTimeoutException, Entity, FetchOptions, Key, Query }
import com.google.appengine.api.memcache.{ MemcacheService, MemcacheServiceFactory }
import dispatch.json._
import java.util.logging.{ Level, Logger }
import javax.servlet.ServletRequest
import scala.collection.JavaConversions._
import sjson.json.JsonSerialization._
import sjson.json.Format
import urashima.talk.lib.util.AppConstants
import org.dotme.liquidtpl.lib.memcache.CounterLogService

object CommentChannelService {
  val MC_KEY_USER = "urashima.talk.service.CommentChannelService#user_";
  val USER_KEY = "u";
  val COUNTER_KEY = "c";
  val USER_KIND_PREFIX = "chU_";
  val CLEANUP_AT_ONCE = 100;

  private val mcService: MemcacheService = MemcacheServiceFactory
    .getMemcacheService();

  private val log: Logger = Logger.getLogger(CommentChannelService.getClass.getName)

  def getUserList(topicNumber: String): List[String] = {
    try {
      val userList = mcService.get(MC_KEY_USER + topicNumber).asInstanceOf[List[String]]
      if (userList != null) {
        userList
      } else {
        log.log(Level.WARNING, "Data from Memcache is null");
        restoreUserList(topicNumber);
      }
    } catch {
      case e =>
        log.log(Level.WARNING, "Failed to fetch from Memcache: ", e);
        // if failed, restore the value from Log
        restoreUserList(topicNumber);
    }
  }

  def getChannelId(cookieUserId: String, topicNumber: String): String = {
    "%s@%s".format(cookieUserId, topicNumber)
  }

  def getTokenJson(request: ServletRequest): JsValue = {
    val cookieUserId: String = request.getAttribute(AppConstants.KEY_COOKIE_USER_ID).asInstanceOf[String]
    val topicId: String = request.getParameter(AppConstants.KEY_TOPIC_ID)
    TopicService.fetchOne(topicId) match {
      case Some(topic) => getTokenJson(cookieUserId, topic.getNumberString)
      case None => JsValue("")
    }
  }

  def getTokenJson(cookieUserId: String, topicNumber: String): JsValue = {
    val channelId: String = getChannelId(cookieUserId, topicNumber)
    updateUserList(cookieUserId, topicNumber)
    JsObject(List(
      (JsString(AppConstants.KEY_CHANNEL_TOKEN), JsString(getToken(channelId))),
      (JsString(AppConstants.KEY_CHANNEL_ID), JsString(channelId))))
  }

  def updateUserList(cookieUserId: String, topicNumber: String) {
    val channelId: String = getChannelId(cookieUserId, topicNumber)
    // try to get the next value from Memcache
    val userList: List[String] = getUserList(topicNumber)
    if (!userList.contains(channelId)) {
      val modifiedUserList: List[String] = channelId :: userList
      // save the new value to Memcache
      mcService.put(MC_KEY_USER + topicNumber, modifiedUserList);
      try {
        saveUserList(modifiedUserList, topicNumber);
      } catch {
        case e: DatastoreTimeoutException => log.log(Level.WARNING, "Failed to save UserList: ", e);
      }
    }
  }

  def getToken(userId: String): String = {
    val channelService: ChannelService = ChannelServiceFactory.getChannelService();
    channelService.createChannel(userId);
  }

  def sendUpdateToUser(userId: String, data: JsValue) {
    val channelService: ChannelService = ChannelServiceFactory.getChannelService();
    channelService.sendMessage(new ChannelMessage(userId, "/*%s*/".format(data.toString)))
  }

  def sendUpdateToUsers(topicNumber: String, data: JsValue) {
    getUserList(topicNumber).foreach { userId =>
      sendUpdateToUser(userId, data)
    }
  }

  // save a count value by a UserList
  private def saveUserList(userList: List[String], topicNumber: String): Unit = {
    val datastoreService = DatastoreServiceFactory.getDatastoreService
    val e: Entity = new Entity(USER_KIND_PREFIX + topicNumber)
    e.setProperty(USER_KEY, asJavaList(userList.toBuffer[String]))
    e.setProperty(COUNTER_KEY, CounterLogService.increment("chU"))
    datastoreService.put(e)
  }

  // restore the largest count value from Datastore
  def restoreUserList(topicNumber: String): List[String] = {
    val datastoreService = DatastoreServiceFactory.getDatastoreService
    val cls: List[Entity] = datastoreService.prepare(new Query(USER_KIND_PREFIX + topicNumber)
      .addSort(COUNTER_KEY, Query.SortDirection.DESCENDING)).asList(
      FetchOptions.Builder.withLimit(1)).toList

    // restore count value if it can
    val userList: List[String] = if (cls == null || cls.size == 0) {
      List[String]()
    } else {
      val jList: java.util.List[String] = cls.apply(0).getProperty(USER_KEY).asInstanceOf[java.util.List[String]]
      asScalaBuffer(jList).toList
    }

    // save the new value to Memcache
    mcService.put(MC_KEY_USER + topicNumber, userList);
    log.log(Level.WARNING, "Restored user list: " + userList);

    // return the value
    return userList;
  }

  /**
   * Deletes the count value stored on Memcache. This should be called only
   * for testing purpose.
   */
  def deleteUserListOnMemcache(topicNumber: String) {
    mcService.delete(MC_KEY_USER + topicNumber);
  }

  def cleanupDatastore(topicNumber: String) {
    var isFirst = true;
    val datastoreService = DatastoreServiceFactory.getDatastoreService
    datastoreService.prepare(new Query(USER_KIND_PREFIX + topicNumber)
      .addSort(COUNTER_KEY, Query.SortDirection.DESCENDING)).asList(
      FetchOptions.Builder.withLimit(CLEANUP_AT_ONCE)).foreach { e =>
        {
          if (!isFirst) {
            datastoreService.delete(e.getKey)
          }
          isFirst = false
        }
      }
  }

}
