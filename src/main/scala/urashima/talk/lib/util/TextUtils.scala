package urashima.talk.lib.util
import java.security.AlgorithmParameters
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

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
      case e:Exception => e.printStackTrace 
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
      case e:Exception => e.printStackTrace 
    }
    return Array[Byte]()
  }
}
