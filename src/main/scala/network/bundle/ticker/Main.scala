package network.bundle.ticker

import java.util.Calendar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.actors.CoinActor
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.markets.b.{Binance, Bitfinex, Bittrex}
import network.bundle.ticker.markets.g.Gemini
import network.bundle.ticker.markets.h.Hitbtc
import network.bundle.ticker.markets.k.Kraken
import network.bundle.ticker.markets.l.Liqui
import network.bundle.ticker.markets.n.NovaExchange
import network.bundle.ticker.markets.p.Poloniex
import network.bundle.ticker.models.Model.{CoinTicker, Increase, Market}
import network.bundle.ticker.models.Tables.Coin
import network.bundle.ticker.services.CoinService

import scala.collection.immutable
import scala.util.{Failure, Success}

object Main extends StrictLogging {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val httpClient = HttpClientFactory.create()
  val mysqlDataSource = MysqlDataSource()
  val service = CoinService(mysqlDataSource)
  val coinActor = system.actorOf(CoinActor.props(service), "CoinActor")

  def runFold(acc: Map[String, BigDecimal], curr: CoinTicker) = {
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

  def main(args: Array[String]): Unit = {
    logger.info("args = {}", args)

    val markets = immutable.Seq(
      Market(Poloniex(httpClient)),
      Market(Binance(httpClient)),
      Market(NovaExchange(httpClient)),
      Market(Kraken(httpClient)),
      Market(Hitbtc(httpClient)),
      Market(Bittrex(httpClient)),
      Market(Gemini(httpClient)),
      Market(Bitfinex(httpClient)),
      Market(Liqui(httpClient))
    ).filter { m =>
      if (args.length != 0) {
        m.market.id == args(0).toInt
       } else {
        m.market.id > 0
      }
    }

    Source(markets)
      .mapAsync(5)(_.market.values())
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

}