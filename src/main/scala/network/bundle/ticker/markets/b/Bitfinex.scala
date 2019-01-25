package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

/**
  * @note Market Api Informations
  * @see https://docs.bitfinex.com
  * @see https://bitfinex.readme.io/v2/docs/rest-public
  * @see https://api.bitfinex.com/v1/symbols
  * @see https://api.bitfinex.com/v1/stats/:symbol
  * @see https://api.bitfinex.com/v1/pubticker/:symbol
  *
  **/
trait Bitfinex extends BaseMarket {

  val TICKER_URL: String = "https://api.bitfinex.com/v1/pubticker"
  val ORDER_URL: String = "https://api.bitfinex.com/v1/book"

  val coinPairs: immutable.Seq[CoinPair] = immutable.Seq(
    CoinPair("ltc", "btc","ltcbtc"),
    CoinPair("eth", "btc","ethbtc"),
    CoinPair("xmr", "btc","xmrbtc"),
    CoinPair("xrp", "btc","xrpbtc"),
    CoinPair("iot", "btc","iotbtc"),
    CoinPair("eos", "btc","eosbtc"),
    CoinPair("bch", "btc","bchbtc"),
    CoinPair("neo", "btc","neobtc")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(coinPairs) { coinPair =>
      val url = s"$TICKER_URL/${coinPair.asset}${coinPair.currency}"

      HttpClientFactory.get(httpClient, url).map { response =>
        val data = parse(response.getResponseBody).values.asInstanceOf[Map[String, String]]
        logger.info("{} - {}", url, data)
        val volume = data("volume")
        val lastPrice = data("last_price")
        CoinTicker(id, coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    Future.traverse(coinPairs) { coinPair =>
      val url = s"$ORDER_URL/${coinPair.asset}${coinPair.currency}"

      HttpClientFactory.get(httpClient, url).map { response =>
        val data = parse(response.getResponseBody).values.asInstanceOf[Map[String, Seq[Map[String, String]]]]

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

object Bitfinex {

  def apply(hc: AsyncHttpClient): Bitfinex = new Bitfinex() {

    override val id = 2L
    override val httpClient: AsyncHttpClient = hc

  }

}