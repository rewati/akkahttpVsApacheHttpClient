package com.ytel.myapp

import akka.actor.{ActorRef, Props}
import com.ytel.miefus.{CommonActor, FaultToleranceSupervision}
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager
import org.apache.http.config.ConnectionConfig
import org.apache.http.impl.nio.conn.ManagedNHttpClientConnectionFactory
import org.apache.http.impl.nio.reactor.{DefaultConnectingIOReactor, IOReactorConfig}
import org.apache.http.nio.conn.ManagedNHttpClientConnection
import org.apache.http.nio.reactor.IOSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

/**
  * Created by rewatiraman.
  */
object HttpSender extends FaultToleranceSupervision {
  override val actorProps: Props = Props[HttpSender]
  override val actorName: String = "HttpSender"
}

class HttpSender extends CommonActor with ApacheHttpClient {

  override def receive = {
    case x: (Int,Long,String) => send(x)
    case _ =>
  }

  def send(x: (Int,Long,String)) = {
    val c = System.currentTimeMillis
    Future({
      client.execute(new HttpGet(x._3), null).get()
    }
    ).onComplete(h => {
      val a = System.currentTimeMillis()
      h match {
        case Success(resp: HttpResponse) if resp.getStatusLine.getStatusCode == 200 =>
          log.info(s"LoadLog ==> Success Resp in time:===> Count: ${x._1} Start: ${x._2} Current: $a Took: ${a - c} ")
        case Success(resp: HttpResponse) =>
          log.error(s"LoadLog ==> Failure Resp in time:===> Count: ${x._1} Start: ${x._2} Current: $a Took:  ${a- c} server code: ${resp.getStatusLine.getStatusCode}")
        case x =>
          log.error(s"LoadLog ==> dont know what to do with $x")
      }
    }
    )
  }

}

trait HttpSenderRequestObject { def url: String }

//TODO Move to Miefus And remove hard codded values
sealed trait ApacheHttpClient {
  val connectionFactory: ManagedNHttpClientConnectionFactory = new ManagedNHttpClientConnectionFactory() {
    override def create(iosession: IOSession, config: ConnectionConfig): ManagedNHttpClientConnection = {
      val conn = super.create(iosession, config)
      conn
    }
  }

  val config = IOReactorConfig.custom().setIoThreadCount(1000)
    .setSoKeepAlive(true).setTcpNoDelay(true).build()

  val ioReactor = new DefaultConnectingIOReactor(config)
  val cm = new PoolingNHttpClientConnectionManager(ioReactor, connectionFactory)
  val client = HttpAsyncClients.custom().setConnectionManager(cm)
    .setMaxConnPerRoute(5000).setMaxConnTotal(5000).build()
  client.start()
}

