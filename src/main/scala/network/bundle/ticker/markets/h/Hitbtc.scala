package network.bundle.ticker.markets.h

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://hitbtc.com/api
  * @see http://api.hitbtc.com/api/1/public/symbols
  * @see http://api.hitbtc.com/api/1/public/:symbol/ticker
  * @see http://api.hitbtc.com/api/1/public/ticker
  */
trait Hitbtc extends BaseMarket {

  val url = "http://api.hitbtc.com/api/1/public/ticker"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, Map[String, String]]]
        .map { ticker =>
          val coinPair = ticker._1 match {
            case x if x.contains("BTC") => CoinPair(x.replace("BTC", "").toLowerCase, "btc", x)
            case x if x.contains("USD") => CoinPair(x.replace("USD", "").toLowerCase, "usd", x)
            case x if x.contains("EUR") => CoinPair(x.replace("EUR", "").toLowerCase, "eur", x)
            case x if x.contains("ETH") => CoinPair(x.replace("ETH", "").toLowerCase, "eth", x)
          }

          val volume = ticker._2("volume_quote")
          val lastPrice = if (ticker._2("last") == null) "0.0" else ticker._2("last")

          CoinTicker("hitbtc", coinPair, BigDecimal(volume), BigDecimal(lastPrice))
        }
        .to[immutable.Seq]
    }
  }

}

object Hitbtc {

  def apply(hc: AsyncHttpClient): Hitbtc = new Hitbtc() {

    override val httpClient: AsyncHttpClient = hc

  }

}