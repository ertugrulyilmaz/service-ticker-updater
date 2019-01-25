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
  * @see https://www.bitstamp.net/api/
  * @see https://www.bitstamp.net/api/ticker/
  * @see https://www.bitstamp.net/api/v2/ticker/btcusd/
  * @see Supported values for currency_pair: btcusd, btceur, xrpusd, xrpeur, xrpbtc, ltcusd, ltceur, ltcbtc
  */
trait Bitstamp extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("btc", "usd"), "https://www.bitstamp.net/api/v2/ticker/btcusd"),
    Tickers(CoinPair("btc", "eur"), "https://www.bitstamp.net/api/v2/ticker/btceur"),
    Tickers(CoinPair("xrp", "usd"), "https://www.bitstamp.net/api/v2/ticker/xrpusd"),
    Tickers(CoinPair("xrp", "eur"), "https://www.bitstamp.net/api/v2/ticker/xrpeur"),
    Tickers(CoinPair("xrp", "btc"), "https://www.bitstamp.net/api/v2/ticker/xrpbtc"),
    Tickers(CoinPair("ltc", "usd"), "https://www.bitstamp.net/api/v2/ticker/ltcusd"),
    Tickers(CoinPair("ltc", "eur"), "https://www.bitstamp.net/api/v2/ticker/ltceur"),
    Tickers(CoinPair("eth", "btc"), "https://www.bitstamp.net/api/v2/ticker/ethbtc"),
    Tickers(CoinPair("eth", "usd"), "https://www.bitstamp.net/api/v2/ticker/ethusd"),
    Tickers(CoinPair("ltc", "btc"), "https://www.bitstamp.net/api/v2/ticker/ltcbtc")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, String]]
        val volume = data("volume")
        val lastPrice = data("last")

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Bitstamp {

  def apply(hc: AsyncHttpClient): Bitstamp = new Bitstamp() {

    override val httpClient: AsyncHttpClient = hc

  }

}