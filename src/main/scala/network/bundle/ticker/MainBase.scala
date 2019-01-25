package network.bundle.ticker

import java.util.concurrent.Semaphore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.ning.http.client.AsyncHttpClient
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.markets.b.{Binance, Bitfinex, Bittrex}
import network.bundle.ticker.markets.g.Gemini
import network.bundle.ticker.markets.h.Hitbtc
import network.bundle.ticker.markets.k.Kraken
import network.bundle.ticker.markets.p.Poloniex
import network.bundle.ticker.markets.y.Yobit
import network.bundle.ticker.models.Model.{Market, ThrottleRequestFilter}

import scala.collection.immutable

trait MainBase extends StrictLogging {

  val config = ConfigFactory.load()
  val mysqlDataSource = MysqlDataSource(config)

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val throttleRequestFilterMap = Map(
    "www.binance.com" -> ThrottleRequestFilter(10L, 500L, new Semaphore(8)),
    "api.bitfinex.com" -> ThrottleRequestFilter(1000L, 2500L, new Semaphore(1)),
    "bittrex.com" -> ThrottleRequestFilter(10L, 500L, new Semaphore(8)),
    "api.gemini.com" -> ThrottleRequestFilter(10L, 500L, new Semaphore(8)),
    "api.hitbtc.com" -> ThrottleRequestFilter(10L, 500L, new Semaphore(8)),
    "api.kraken.com" -> ThrottleRequestFilter(10L, 500L, new Semaphore(8)),
    "api.liqui.io" -> ThrottleRequestFilter(10L, 500L, new Semaphore(1)),
    "poloniex.com" -> ThrottleRequestFilter(10L, 500L, new Semaphore(8)),
    "yobit.net" -> ThrottleRequestFilter(10L, 500L, new Semaphore(5))
  )

  val httpClient: AsyncHttpClient = HttpClientFactory.create(throttleRequestFilterMap)

  def markets(args: Array[String]): immutable.Seq[Market] = {
    logger.info("args = {}", args)

    immutable.Seq(
      Market(Binance(httpClient)),
      Market(Bitfinex(httpClient)),
      Market(Bittrex(httpClient)),
      Market(Gemini(httpClient)),
      Market(Hitbtc(httpClient)),
      Market(Kraken(httpClient)),
      Market(Poloniex(httpClient)),
      Market(Yobit(httpClient))
    ).filter { m =>
      if (args.length != 0) {
        args.contains(m.exchange.id.toString)
      } else {
        m.exchange.id > 0
      }
    }
  }

}
