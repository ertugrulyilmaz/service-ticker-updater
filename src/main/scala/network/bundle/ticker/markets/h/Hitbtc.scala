package network.bundle.ticker.markets.h

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
  * @see https://hitbtc.com/api
  * @see http://api.hitbtc.com/api/1/public/symbols
  * @see http://api.hitbtc.com/api/1/public/:symbol/ticker
  * @see http://api.hitbtc.com/api/1/public/ticker
  */
trait Hitbtc extends BaseMarket {

  val PAIRS_URL = "http://api.hitbtc.com/api/1/public/symbols"
  val TICKER_URL = "http://api.hitbtc.com/api/1/public/ticker"
  val ORDER_URL = "http://api.hitbtc.com/api/1/public"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, TICKER_URL).map { res =>
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

          CoinTicker(id, coinPair, BigDecimal(volume), BigDecimal(lastPrice))
        }
        .to[immutable.Seq]
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    val coinPairs = parse(HttpClientFactory.completedGet(httpClient, PAIRS_URL).getResponseBody)
      .values
      .asInstanceOf[Map[String, Seq[Map[String, String]]]]("symbols")
      .map { symbols =>
        val asset = symbols("commodity")
        val currency = symbols("currency")

        CoinPair(asset, currency, s"$asset$currency")
      }
      .filter(_.currency == "BTC")
      .filter(x => x.asset == "EOS" || x.asset == "XLM" || x.asset == "XMR" || x.asset == "ETH" || x.asset == "LTC" || x.asset == "ADA" || x.asset == "BCH" || x.asset == "NEO" || x.asset == "XRP" || x.asset == "DASH")
      .to[immutable.Seq]

    Future.traverse(coinPairs) { coinPair =>
      HttpClientFactory.get(httpClient, s"$ORDER_URL/${coinPair.market}/orderbook").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Seq[Seq[String]]]]

        val bids = data.getOrElse("bids", Seq.empty[Seq[String]])
          .map { bid =>
            val price = bid.head
            val amount = bid.last

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        val asks = data.getOrElse("asks", Seq.empty[Seq[String]])
          .map { ask =>
            val price = ask.head
            val amount = ask.last

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }
    }
  }

}

object Hitbtc {

  def apply(hc: AsyncHttpClient): Hitbtc = new Hitbtc() {

    override val id = 6L
    override val httpClient: AsyncHttpClient = hc

  }

}