package com.ytel.myapp

import com.ytel.miefus.{Configuration, MiefusApplication}
import com.ytel.myapp.Application.start

/**
  * Start writing your message handler..
  */

object Application extends MiefusApplication {

  val start = System.currentTimeMillis()
  val loadSize: Int = Configuration("total_load",4000)
  val url = Configuration("total_load",
    """http://preprod-fake-kannel.lb.ytel.com/psid/9a20c508-cb93-377c-7f21-1b620602f249/so""")
//  HttpSenderTest.initialize(loadSize,url)
  AkkaHttpClientTest.initialize(loadSize,url)
}

object HttpSenderTest {
  def initialize(loadSize: Int,url: String) = {
    for (i <- 1 to loadSize) HttpSender.actor ! (i, start, url)
  }
}

object AkkaHttpClientTest {

  case class Action(url: String, currentCount: Int, start: Long) extends HttpRequestTrait {
    val handler = (resp: AkkaHttpSenderResponse) => resp match {
      case x: AkkaHttpSenderSuccessResponse if x.response.status.intValue == 200 =>
        println(s"LoadLog ==> Success Resp in time:===> Count: $currentCount Start: $start Current: ${x.completed} Took: ${x.executionTime} ")
      case x: AkkaHttpSenderSuccessResponse =>
        println(s"LoadLog ==> Failure Resp in time:===> Count: $currentCount Start: $start Current: ${x.completed} Took: ${x.executionTime} Server code: ${x.response.status.intValue()}")
      case x: AkkaHttpSenderFailedResponse =>
        println(s"LoadLog ==> Failure:===> Count: $currentCount Start: $start Current: ${x.completed} Took: ${x.executionTime} Exception: ${x.exception.getMessage}")
    }
  }

  def initialize(loadSize: Int,url: String) = {
    val start = System.currentTimeMillis()
    for (i <- 1 to loadSize) AkkaHttpSender.actor ! Action(url,i,start)
  }

}




