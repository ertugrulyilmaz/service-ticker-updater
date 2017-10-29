package network.bundle.ticker.markets.g

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://docs.gemini.com/rest-api/#symbols
  * @see https://api.gemini.com/v1/symbols
  * @see https://api.gemini.com/v1/pubticker/:symbol
  */
trait Gemini extends BaseMarket {

  val url = ""
  val tickers = immutable.Seq(
    Tickers(CoinPair("btc", "usd", "btcusd"), "https://api.gemini.com/v1/pubticker/btcusd"),
    Tickers(CoinPair("eth", "btc", "ethbtc"), "https://api.gemini.com/v1/pubticker/ethbtc"),
    Tickers(CoinPair("eth", "usd", "ethusd"), "https://api.gemini.com/v1/pubticker/ethusd")
  )

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers.filter(_.coinPair.currency == "btc")) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Any]]
        val volume = data("volume").asInstanceOf[Map[String, String]](ticker.coinPair.currency.toUpperCase)
        val lastPrice = data("last").toString

        CoinTicker("gemini", ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
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