package network.bundle.ticker

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import network.bundle.ticker.services.OrderBookService
import org.json4s.DefaultFormats

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object MainOrders extends MainBase with App {

  implicit val formats = DefaultFormats

  val orderBookService = OrderBookService(mysqlDataSource)

  Source(markets(args))
    .mapAsync(5) { market =>
      orderBookService.deleteByExchange(market.exchange.id)

      market.exchange.orders()
    }
    .mapConcat(identity)
    .filter(orderBookService.filterOrders)
    .buffer(10, OverflowStrategy.backpressure)
    .groupedWithin(50, 100.milliseconds)
    .runForeach(orderBookService.saveOrderBook)
    .onComplete {
      case Success(_) =>
        logger.info("completed!..")
      //          system.terminate().onComplete(_ => System.exit(0))
      case Failure(cause) =>
        logger.error("Failed", cause)
        system.terminate().onComplete(_ => System.exit(1))
    }

}