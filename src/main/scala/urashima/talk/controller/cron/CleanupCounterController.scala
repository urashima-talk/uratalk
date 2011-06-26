package urashima.talk.controller.cron

import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.FetchOptions
import com.google.appengine.api.datastore.Query
import org.dotme.liquidtpl.lib.memcache.ReverseCounterLogService
import org.dotme.liquidtpl.lib.memcache.CounterLogService
import urashima.talk.service.TopicService
import org.slim3.controller.Controller
import org.slim3.controller.Navigation
import scala.collection.JavaConversions._

class CleanupcounterController extends Controller {

  @throws(classOf[Exception])
  override def run(): Navigation = {
    ReverseCounterLogService.cleanupDatastore("t")
    ReverseCounterLogService.cleanupDatastore("c")
    CounterLogService.cleanupDatastore("t_all")
    //Todo paginate
    TopicService.fetchAll.foreach { topic =>
      CounterLogService.cleanupDatastore("c_%s".format(topic.getNumberString))
    }
    null
  }
}
