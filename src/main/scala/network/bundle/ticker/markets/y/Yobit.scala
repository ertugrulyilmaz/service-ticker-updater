package network.bundle.ticker.markets.y

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

/**
  * @note documentation is below.
  * @see https://www.yobit.net/en/api/
  * @see https://yobit.net/en/coinsinfo/
  * @see https://yobit.net/api/3/info
  * @see https://yobit.net/api/3/ticker/ltc_btc
  */
trait Yobit extends BaseMarket {

  val PAIRS_URL = "https://yobit.net/api/3/info"
  val TICKER_URL = "https://yobit.net/api/3/ticker"
  val ORDER_URL = "https://yobit.net/api/3/depth"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(pairs()) { coinPair =>
      HttpClientFactory.get(httpClient, s"$TICKER_URL/${coinPair.market}").map { resPair =>
        val data = parse(resPair.getResponseBody)
          .values.asInstanceOf[Map[String, Map[String, Any]]](coinPair.market)

        val volume = data("vol_cur").toString
        val lastPrice = data("last").toString

        CoinTicker(id, coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    Future.traverse(pairs()) { coinPair =>
      HttpClientFactory.get(httpClient, s"$ORDER_URL/${coinPair.market}").map { res =>

        val data = parse(res.getResponseBody)
          .values.asInstanceOf[Map[String, Map[String, Seq[Seq[Any]]]]](coinPair.market)

        val bids = data.getOrElse("bids", Seq.empty[Seq[Any]])
          .map { bid =>
            val price = bid.head.toString
            val amount = bid.last.toString

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        val asks = data.getOrElse("asks", Seq.empty[Seq[Any]])
          .map { ask =>
            val price = ask.head.toString
            val amount = ask.last.toString

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }
    }
  }

  private def pairs(): immutable.Seq[CoinPair] = {
    parse(HttpClientFactory.completedGet(httpClient, PAIRS_URL).getResponseBody)
      .values.asInstanceOf[Map[String, Map[String, Any]]]("pairs")
      .keys
      .filter(_.endsWith("_btc"))
      .filter(x => x.startsWith("eos") || x.startsWith("ltc_") || x.startsWith("eth") || x.startsWith("bcc") || x.startsWith("xrp") || x.startsWith("dash"))
      .map { pair =>
        val assetCurrency = pair.split("_")
        val asset = reformatCurrencyName(assetCurrency.head)
        val currency = reformatCurrencyName(assetCurrency.last)

        CoinPair(asset, currency, pair)
      }
      .toList
  }

}

object Yobit {

  def apply(hc: AsyncHttpClient): Yobit = new Yobit() {

    override def id: Long = 11L

    override val httpClient: AsyncHttpClient = hc

  }

}