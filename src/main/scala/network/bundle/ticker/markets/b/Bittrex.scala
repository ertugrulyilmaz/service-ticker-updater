package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model._
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

/**
  * @note documentation is below
  * @see https://bittrex.com/Home/Api
  * @see https://bittrex.com/api/v1.1/public/getmarketsummaries
  */
trait Bittrex extends BaseMarket {

  val TICKER_URL = "https://bittrex.com/api/v1.1/public/getmarketsummaries"
  val ORDER_URL = "https://bittrex.com/api/v1.1/public/getorderbook"

  val coinPairs: immutable.Seq[CoinPair] = immutable.Seq(
    CoinPair("eth", "btc", "BTC-ETH"),
    CoinPair("xrp", "btc", "BTC-XRP"),
    CoinPair("ltc", "btc", "BTC-LTC"),
    CoinPair("bch", "btc", "BTC-BCH"),
    CoinPair("eos", "btc", "BTC-EOS"),
    CoinPair("ada", "btc", "BTC-ADA"),
    CoinPair("xlm", "btc", "BTC-XLM"),
    CoinPair("neo", "btc", "BTC-NEO"),
    CoinPair("dash", "btc", "BTC-DASH"),
    CoinPair("xmr", "btc", "BTC-XMR")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, TICKER_URL).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, List[Map[String, Any]]]]("result")
        .map { pair =>
          val marketName = pair("MarketName").toString.toLowerCase.split("-")
          val asset = reformatCurrencyName(marketName.last.toLowerCase)
          val currency = reformatCurrencyName(marketName.head.toLowerCase)

          val volume = pair("Volume").toString
          val lastPrice = pair("Last").toString

          CoinTicker(id, CoinPair(asset, currency, pair("MarketName").toString), BigDecimal(volume), BigDecimal(lastPrice))
        }
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    Future.traverse(coinPairs) { coinPair =>
      val bids = HttpClientFactory.get(httpClient, s"$ORDER_URL?type=sell&market=${coinPair.market}").map { res =>
        parse(res.getResponseBody).values.asInstanceOf[Map[String, Seq[Map[String, Double]]]]
          .getOrElse("result", Seq.empty[Map[String, Double]])
          .map { bid =>
            val price = bid.getOrElse("Rate", 0.0).toString
            val amount = bid.getOrElse("Quantity", 0.0).toString

            Order(BigDecimal(price), BigDecimal(amount))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)
      }

      val asks = HttpClientFactory.get(httpClient, s"$ORDER_URL?type=buy&market=${coinPair.market}").map { res =>
        parse(res.getResponseBody).values.asInstanceOf[Map[String, Seq[Map[String, Double]]]]
          .getOrElse("result", Seq.empty[Map[String, Double]])
          .map { bid =>
            val price = bid.getOrElse("Rate", 0.0).toString
            val amount = bid.getOrElse("Quantity", 0.0).toString

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)
      }

      for {
        a <- asks
        b <- bids
      } yield ExchangeCoinOrders(id, CoinOrder(coinPair, a), CoinOrder(coinPair, b))
    }
  }

}

object Bittrex {

  def apply(hc: AsyncHttpClient): Bittrex = new Bittrex() {

    override val id = 3L
    override val httpClient: AsyncHttpClient = hc

  }

}