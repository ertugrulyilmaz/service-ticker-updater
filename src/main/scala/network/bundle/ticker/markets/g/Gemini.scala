package network.bundle.ticker.markets.g

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

/**
  * @note documentation is below
  * @see https://docs.gemini.com/rest-api/#symbols
  * @see https://api.gemini.com/v1/symbols
  * @see https://api.gemini.com/v1/pubticker/:symbol
  */
trait Gemini extends BaseMarket {

  val TICKER_URL = "https://api.gemini.com/v1/pubticker"
  val ORDER_URL = "https://api.gemini.com/v1/book"

  val pairs = immutable.Seq(
    CoinPair("btc", "usd", "btcusd"),
    CoinPair("eth", "btc", "ethbtc"),
    CoinPair("eth", "usd", "ethusd")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(pairs.filter(_.currency == "btc")) { coinPair =>
      HttpClientFactory.get(httpClient, s"$TICKER_URL/${coinPair.market}").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Any]]
        val volume = data("volume").asInstanceOf[Map[String, String]](coinPair.currency.toUpperCase)
        val lastPrice = data("last").toString

        CoinTicker(id, coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    Future.traverse(pairs.filter(_.currency == "btc")) { coinPair =>
      HttpClientFactory.get(httpClient, s"$ORDER_URL/${coinPair.market}").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Seq[Map[String, String]]]]

        val bids = data.getOrElse("bids", Seq.empty[Map[String, String]])
          .map { bid =>
            val price = bid.getOrElse("price", "0.0")
            val amount = bid.getOrElse("amount", "0.0")

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        val asks = data.getOrElse("asks", Seq.empty[Map[String, String]])
          .map { bid =>
            val price = bid.getOrElse("price", "0.0")
            val amount = bid.getOrElse("amount", "0.0")

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }
    }
  }
}

object Gemini {

  def apply(hc: AsyncHttpClient): Gemini = new Gemini() {

    override val id = 5L
    override val httpClient: AsyncHttpClient = hc

  }

}