package urashima.talk.lib.util
import java.util.TimeZone
import java.text.DateFormat
import java.text.SimpleDateFormat

object AppConstants {
  val CONFIG_FOLDER = "contents"
  val CONFIG_FILE = "app"
  val SYSTEM_TIME_ZONE: TimeZone = TimeZone.getTimeZone("GMT-8:00")
  val COMMON_KEY: String = "VC5GPCvuRR1YMWod";

  // settings for validate
  val VALIDATE_STRING_LENGTH = 100
  val VALIDATE_LONGTEXT_LENGTH = 100000

  val KEY_TOPIC_ID = "topicId"
  val KEY_CHANNEL_TOKEN = "channelToken"
  val KEY_CHANNEL_ID = "channelId"
  val KEY_COOKIE_USER_ID = "cookieUserId"

  private val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Tokyo")
  def timeZone: TimeZone = DEFAULT_TIME_ZONE;

  def dayCountFormat: DateFormat = {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyyMMdd")
    dateFormat.setTimeZone(AppConstants.SYSTEM_TIME_ZONE)
    dateFormat
  }

  def dayCountFormatWithTimeZone(timeZone: TimeZone): DateFormat = {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyyMMdd")
    dateFormat.setTimeZone(timeZone)
    dateFormat
  }

  def monthCountFormatWithTimeZone(timeZone: TimeZone): DateFormat = {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyyMM")
    dateFormat.setTimeZone(timeZone)
    dateFormat
  }

  def yearCountFormatWithTimeZone(timeZone: TimeZone): DateFormat = {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyy")
    dateFormat.setTimeZone(timeZone)
    dateFormat
  }

  def timeFormat: DateFormat = {
    val dateFormat: DateFormat = new SimpleDateFormat("HH:mm")
    dateFormat.setTimeZone(AppConstants.timeZone)
    dateFormat
  }

  def dateTimeFormat: DateFormat = {
    val dateFormat: DateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm")
    dateFormat.setTimeZone(AppConstants.timeZone)
    dateFormat
  }
}
