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
  * @note documentation is below. available markets are ETHBTC, LTCBTC, BNBBTC, NEOBTC, BCCBTC, EOSETH, SNTETH, BNBETH, QTUMETH
  * @see https://www.binance.com/restapipub.html
  * @see https://www.binance.com/api/v1/ticker/24hr?symbol=BTCCNY
  */
trait Binance extends BaseMarket {

  val TICKER_URL = "https://www.binance.com/api/v1/ticker/24hr?symbol="
  val ORDER_URL = "https://www.binance.com/api/v1/depth?symbol="

  val coinPairs: immutable.Seq[CoinPair] = immutable.Seq(
    CoinPair("eth", "btc", "ETHBTC"),
    CoinPair("xrp", "btc", "XRPBTC"),
    CoinPair("bch", "btc", "BCHBTC"),
    CoinPair("ltc", "btc", "LTCBTC"),
    CoinPair("eos", "btc", "EOSBTC"),
    CoinPair("ada", "btc", "ADABTC"),
    CoinPair("xlm", "btc", "XLMBTC"),
    CoinPair("neo", "btc", "NEOBTC"),
    CoinPair("iota", "btc", "IOTABTC"),
    CoinPair("xmr", "btc", "XMRBTC"),
    CoinPair("dash", "btc", "DASHBTC")
    )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(coinPairs) { coinPair =>
      HttpClientFactory.get(httpClient, s"$TICKER_URL${coinPair.market}").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, String]]
        val volume = data.getOrElse("volume", "0.0")
        val lastPrice = data.getOrElse("lastPrice", "0.0")

        CoinTicker(id, coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    Future.traverse(coinPairs) { coinPair =>
      HttpClientFactory.get(httpClient, s"$ORDER_URL${coinPair.market}").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Seq[Seq[String]]]]

        val bids = data.getOrElse("bids", Seq.empty[Seq[String]])
          .map { bid =>
            val price = bid.head
            val amount = bid(1)

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount  > 0)
          .take(ORDER_LIMIT)

        val asks = data.getOrElse("asks", Seq.empty[Seq[String]])
          .map { ask =>
            val price = ask.head
            val amount = ask(1)

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount  > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }
    }
  }

}

object Binance {

  def apply(hc: AsyncHttpClient): Binance = new Binance() {

    override val id = 1L
    override val httpClient: AsyncHttpClient = hc

  }

}

