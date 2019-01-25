package network.bundle.ticker

import java.util.Calendar

import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import network.bundle.ticker.actors.CoinActor
import network.bundle.ticker.models.Model.{CoinTicker, Increase}
import network.bundle.ticker.models.Tables.Coin
import network.bundle.ticker.services.CoinService

import scala.util.{Failure, Success}

object MainTicker extends MainBase with App {

  def runFold(acc: Map[String, BigDecimal], curr: CoinTicker): Map[String, BigDecimal] = {
    coinActor ! curr

    val lastPrice = acc.getOrElse(curr.pair.name, BigDecimal(Double.MaxValue))

    if (curr.lastPrice < lastPrice) {
      acc ++ Map(curr.pair.name -> curr.lastPrice)
    } else {
      acc
    }
  }

  def isValidTicker(coinTicker: CoinTicker): Boolean = {
    coinTicker.pair.currency == "btc" && coinTicker.lastPrice > 0 && coinTicker.volume > 0
  }

  val service = CoinService(mysqlDataSource)
  val coinActor: ActorRef = system.actorOf(CoinActor.props(service), "CoinActor")

  Source(markets(args))
    .mapAsync(5)(_.exchange.tickers())
    .mapConcat(identity)
    .filter(isValidTicker)
    .runFold(Map.empty[String, BigDecimal])(runFold)
    .onComplete {
      case Success(result) =>
        result
          .foreach { data =>
            val assetCurrency = data._1.split("-")

            coinActor ! Increase
            coinActor ! Coin(assetCurrency.head, data._2, Calendar.getInstance().getTimeInMillis)
          }
      case Failure(cause) =>
        logger.error("Failed", cause)
        system.terminate().onComplete(_ => System.exit(1))
    }

}