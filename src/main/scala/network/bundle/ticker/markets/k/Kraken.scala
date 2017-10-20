package network.bundle.ticker.markets.k

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation link is below
  * @see https://www.kraken.com/en-us/help/api#public-market-data
  * @see https://api.kraken.com/0/public/AssetPairs?info=info
  * @see https://api.kraken.com/0/public/Ticker?pair=BCHUSD
  * @note <pair_name> = pair name
  *       a = ask array(<price>, <whole lot volume>, <lot volume>),
  *       b = bid array(<price>, <whole lot volume>, <lot volume>),
  *       c = last trade closed array(<price>, <lot volume>),
  *       v = volume array(<today>, <last 24 hours>),
  *       p = volume weighted average price array(<today>, <last 24 hours>),
  *       t = number of trades array(<today>, <last 24 hours>),
  *       l = low array(<today>, <last 24 hours>),
  *       h = high array(<today>, <last 24 hours>),
  *       o = today's opening price
  */
trait Kraken extends BaseMarket {

  val pairsUrl = "https://api.kraken.com/0/public/AssetPairs?info=info"
  val tickerUrl = "https://api.kraken.com/0/public/Ticker"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val res = HttpClientFactory.completedGet(httpClient, pairsUrl)
    val vals = parse(res.getResponseBody)
      .values
      .asInstanceOf[Map[String, Map[String, Map[String, String]]]]("result")
      .values.to[immutable.Seq]

    Future.traverse(vals) { pair =>
      val altname = pair("altname").replace(".d", "")
      val base = if (pair("base").startsWith("X")) pair("base").tail else pair("base")
      val quote = if (pair("quote").startsWith("Z") || pair("quote").startsWith("X")) pair("quote").tail else pair("quote")

      // TODO: zero degerler filtrelenmeli
      HttpClientFactory.get(httpClient, s"${tickerUrl}?pair=${altname}").map { res2 =>
        parse(res2.getResponseBody)
          .values
          .asInstanceOf[Map[String, Map[String, Map[String, Seq[String]]]]]("result")
          .values match {
          case data if data.isEmpty =>
            CoinTicker("kraken", CoinPair("empty", "empty", "empty"), zero, zero)
          case data =>
            val volume = data.head("v").last
            val lastPrice = data.head("p").last
            val asset = reformatCurrencyName(base.toLowerCase)
            val currency = reformatCurrencyName(quote.toLowerCase)

            CoinTicker("kraken", CoinPair(asset, currency, altname), BigDecimal(volume), BigDecimal(lastPrice))
        }
      }
    }
  }

}

object Kraken {

  def apply(hc: AsyncHttpClient): Kraken = new Kraken() {

    override val httpClient: AsyncHttpClient = hc

  }

}