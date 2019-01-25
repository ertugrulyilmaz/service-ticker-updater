package network.bundle.ticker.markets.k

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

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

  val PAIRS_URL = "https://api.kraken.com/0/public/AssetPairs"
  val TICKER_URL = "https://api.kraken.com/0/public/Ticker"
  val ORDER_URL = "https://api.kraken.com/0/public/Depth"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(pairs()) { pair =>
      val altname = pair("altname").replace(".d", "")
      val base = if (pair("base").startsWith("X")) pair("base").tail else pair("base")
      val quote = if (pair("quote").startsWith("Z") || pair("quote").startsWith("X")) pair("quote").tail else pair("quote")
      val asset = reformatCurrencyName(base.toLowerCase)
      val currency = reformatCurrencyName(quote.toLowerCase)
      val coinPair = CoinPair(asset, currency, altname)

      // TODO: zero degerler filtrelenmeli
      HttpClientFactory.get(httpClient, s"$TICKER_URL?pair=$altname").map { res2 =>
        parse(res2.getResponseBody)
          .values
          .asInstanceOf[Map[String, Map[String, Map[String, Seq[String]]]]]("result")
          .values match {
          case data if data.isEmpty =>
            CoinTicker(id, CoinPair("empty", "empty", "empty"), zero, zero)
          case data =>
            val volume = data.head("v").last
            val lastPrice = data.head("c").head

            CoinTicker(id, coinPair, BigDecimal(volume), BigDecimal(lastPrice))
        }
      }
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    Future.traverse(pairs()) { pair =>
      val altname = pair("altname").replace(".d", "")
      val base = if (pair("base").startsWith("X")) pair("base").tail else pair("base")
      val quote = if (pair("quote").startsWith("Z") || pair("quote").startsWith("X")) pair("quote").tail else pair("quote")
      val asset = reformatCurrencyName(base.toLowerCase)
      val currency = reformatCurrencyName(quote.toLowerCase)
      val coinPair = CoinPair(asset, currency, altname)

      HttpClientFactory.get(httpClient, s"$ORDER_URL?pair=$altname").map { res2 =>
        val data = parse(res2.getResponseBody)
          .values
          .asInstanceOf[Map[String, Map[String, Map[String, Seq[Seq[String]]]]]]("result").head._2

        val bids = data.getOrElse("bids", Seq.empty[Seq[String]])
          .map { bid =>
            val price = bid.head
            val amount = bid(1)

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        val asks = data.getOrElse("asks", Seq.empty[Seq[String]])
          .map { bid =>
            val price = bid.head
            val amount = bid(1)

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }
    }
  }

  private def pairs(): immutable.Seq[Map[String, String]] = {
    parse(HttpClientFactory.completedGet(httpClient, PAIRS_URL).getResponseBody)
      .values
      .asInstanceOf[Map[String, Map[String, Map[String, String]]]]("result")
      .values.to[immutable.Seq].filter(_("quote").contains("XBT"))
  }

}

object Kraken {

  def apply(hc: AsyncHttpClient): Kraken = new Kraken() {

    override val id = 7L
    override val httpClient: AsyncHttpClient = hc

  }

}