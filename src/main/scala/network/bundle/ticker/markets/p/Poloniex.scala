package network.bundle.ticker.markets.p

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
  * @see https://poloniex.com/support/api/
  * @see https://poloniex.com/public?command=returnTicker
  * @see https://poloniex.com/public?command=return24hVolume
  *
  **/
trait Poloniex extends BaseMarket {

  val TICKER_URL = "https://poloniex.com/public?command=returnTicker"
  val ORDER_URL = "https://poloniex.com/public?command=returnOrderBook&currencyPair=all"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, TICKER_URL).map { res =>
      parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, String]]].map { data =>
        val pair = data._1.toLowerCase.split("_")
        val asset = reformatCurrencyName(pair.last)
        val currency = reformatCurrencyName(pair.head)
        val volume = data._2("quoteVolume")
        val lastPrice = data._2("last")

        CoinTicker(id, CoinPair(asset, currency, data._1), BigDecimal(volume), BigDecimal(lastPrice))
      }.to[immutable.Seq]
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    HttpClientFactory.get(httpClient, ORDER_URL).map { res =>
      parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Seq[Seq[Any]]]]].map { data =>
        val assetCurrency = data._1.toLowerCase.split("_")
        val coinPair = CoinPair(reformatCurrencyName(assetCurrency.last), reformatCurrencyName(assetCurrency.head), data._1)
        val orderBooks = data._2

        val bids = orderBooks.getOrElse("bids", Seq.empty[Seq[Any]])
          .map { bid =>
            val price = bid.head.toString
            val amount = bid.last.toString

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        val asks = orderBooks.getOrElse("asks", Seq.empty[Seq[Any]])
          .map { ask =>
            val price = ask.head.toString
            val amount = ask.last.toString

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }.to[immutable.Seq]
    }
  }

}

object Poloniex {

  def apply(hc: AsyncHttpClient): Poloniex = new Poloniex() {

    override val id = 10L
    override val httpClient: AsyncHttpClient = hc

  }

}
