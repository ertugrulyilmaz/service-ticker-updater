package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://bitflyer.jp/API?top_link&footer
  * @see https://lightning.bitflyer.jp/docs?lang=en
  * @see https://api.bitflyer.jp/v1/markets
  * @see https://api.bitflyer.jp/v1/ticker?product_code=BTC_JPY
  * @see https://api.bitflyer.jp/v1/ticker?product_code=ETH_BTC
  */
trait Bitflyer extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("btc", "jpy"), "https://api.bitflyer.jp/v1/ticker?product_code=BTC_JPY"),
    Tickers(CoinPair("eth", "btc"), "https://api.bitflyer.jp/v1/ticker?product_code=ETH_BTC")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Double]]]("data")
        val volume = data("volume")
        val lastPrice = data("best_bid")

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }
}

object Bitflyer {

  def apply(hc: AsyncHttpClient): Bitflyer = new Bitflyer() {

    override val httpClient: AsyncHttpClient = hc

  }

}
