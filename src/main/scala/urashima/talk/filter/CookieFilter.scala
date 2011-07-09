/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package urashima.talk.filter

import java.io.IOException
import java.security.Principal
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.google.appengine.api.users.UserService
import com.google.appengine.api.users.UserServiceFactory
import scala.collection.mutable.ListBuffer
import javax.servlet.http.Cookie
import java.util.UUID
import urashima.talk.lib.util.AppConstants

class CookieFilter extends Filter {
  val COOKIE_MAX_AGE = 86400 * 7
  @throws(classOf[ServletException])
  override def init(config: FilterConfig): Unit = {
  }

  override def destroy(): Unit = {
  }

  @throws(classOf[IOException])
  @throws(classOf[ServletException])
  override def doFilter(request: ServletRequest, response: ServletResponse,
    chain: FilterChain): Unit = {
    val cookies: Array[Cookie] = request.asInstanceOf[HttpServletRequest].getCookies
    val cookieUserId: String = if (cookies == null || cookies.size == 0) {
      createCookieUserId(request, response)
    } else {
      cookies.find { cookie: Cookie =>
        cookie.getName == AppConstants.KEY_COOKIE_USER_ID
      } match {
        case Some(cookie) => {
          putCookie(cookie, response)
          cookie.getValue
        }
        case None => {
          createCookieUserId(request, response)
        }
      }
    }
    request.setAttribute(AppConstants.KEY_COOKIE_USER_ID, cookieUserId)
    chain.doFilter(request, response);
  }

  def createCookieUserId(request: ServletRequest, response: ServletResponse): String = {
    val uid: String = UUID.randomUUID().toString()
    val newCookie: Cookie = new Cookie(AppConstants.KEY_COOKIE_USER_ID, uid)
    putCookie(newCookie, response)
    uid
  }

  def putCookie(cookie: Cookie, response: ServletResponse): Unit = {
    cookie.setPath("/")
    cookie.setMaxAge(COOKIE_MAX_AGE);
    response.asInstanceOf[HttpServletResponse].addCookie(cookie);
  }
}
