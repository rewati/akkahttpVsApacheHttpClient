package com.ytel.myapp

import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import com.ytel.miefus.{ClusterCommon, CommonActor, FaultToleranceSupervision}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Created by rewatiraman.
  */
object AkkaHttpSender extends FaultToleranceSupervision {
  override val actorProps: Props = Props[AkkaHttpSender]
  override val actorName: String = "AkkaHttpSender"
}
class AkkaHttpSender extends CommonActor {
  implicit val system = ClusterCommon.system
  implicit val materializer = ActorMaterializer()
  override def receive = {
    case x: HttpRequestTrait => responseFuture(x)
  }

  def responseFuture(x: HttpRequestTrait): Unit = {
    val start = System.currentTimeMillis()
    Http().singleRequest(HttpRequest(HttpMethods.GET, x.url)) onComplete {
      case Success(res) => x.handler(AkkaHttpSenderSuccessResponse(res,start))
      case Failure(e) => x.handler(AkkaHttpSenderFailedResponse(e,start))
    }
  }
}

trait HttpRequestTrait {
  val url: String
  val handler: ((AkkaHttpSenderResponse) => Unit)
}


trait AkkaHttpSenderResponse {
  val start: Long
  val completed = System.currentTimeMillis()
  def executionTime = completed-start
}
case class AkkaHttpSenderSuccessResponse(response: HttpResponse,start: Long ) extends AkkaHttpSenderResponse
case class AkkaHttpSenderFailedResponse(exception: Throwable,start: Long ) extends AkkaHttpSenderResponse
