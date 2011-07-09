package urashima.talk.lib.util
import java.security.AlgorithmParameters
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64
import scala.xml.{ NodeSeq, XML, Text }

object TextUtils {

  def encode(data: String): String = {
    val bytes = encodeBytes(data.getBytes, makeKey(AppConstants.COMMON_KEY))
    Base64.encodeBase64String(bytes)
  }

  def decode(data: String): String = {
    val bytes = decodeBytes(Base64.decodeBase64(data), makeKey(AppConstants.COMMON_KEY))
    new String(bytes)
  }

  /**
   * 秘密鍵をバイト列から生成する
   * @param key_bits 鍵の長さ（ビット単位）
   */
  def makeKey(key: String): Key = {
    return new SecretKeySpec(key.getBytes, "AES");
  }

  /**
   * 暗号化
   */
  def encodeBytes(src: Array[Byte], skey: Key): Array[Byte] = {
    try {
      val cipher: Cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, skey);
      return cipher.doFinal(src);
    } catch {
      case e: Exception => e.printStackTrace
    }
    return Array[Byte]()
  }

  /**
   * 復号化
   */
  def decodeBytes(src: Array[Byte], skey: Key): Array[Byte] = {
    try {
      val cipher: Cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, skey);
      return cipher.doFinal(src);
    } catch {
      case e: Exception => e.printStackTrace
    }
    return Array[Byte]()
  }

  def textToHtml(text: String): NodeSeq = {
    text.replaceAll("\r\n", "\n").split("\r|\n|\r\n").flatMap { line =>
      <p>{ autoLink(line) }</p>
    }.toSeq
  }

  def autoLink(text: String): NodeSeq = {
    val URLPATTERN = "((http|https|ftp):\\/\\/[\\w?=&.\\/-;#~%-]+(?![\\w\\s?&.\\/;#~%\"=-]*>))".r
    URLPATTERN.findFirstMatchIn(text) match {
      case None => Text(text)
      case Some(m) =>
        <xml:group>{ Text(m.before.toString) }<a target="_blank" class="ui-link" href={ m.matched.toString }>{ m.matched.toString }</a>{ autoLink(m.after.toString) }</xml:group>
    }
  }
}
