package urashima.talk.controller.cron

import org.dotme.liquidtpl.lib.memcache.{CounterLogService, ReverseCounterLogService}
import org.slim3.controller.{Controller, Navigation}
import scala.collection.JavaConversions._

class CleanupcounterController extends Controller {

  @throws(classOf[Exception])
  override def run(): Navigation = {
    ReverseCounterLogService.cleanupDatastore("t")
    ReverseCounterLogService.cleanupDatastore("c")
    CounterLogService.cleanupDatastore("t_all")
    null
  }
}
