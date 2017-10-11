package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. available markets are ETHBTC, LTCBTC, BNBBTC, NEOBTC, BCCBTC, EOSETH, SNTETH, BNBETH, QTUMETH
  * @see https://www.binance.com/restapipub.html
  * @see https://www.binance.com/api/v1/ticker/24hr?symbol=BTCCNY
  */
trait Binance extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("eth", "btc"), "https://www.binance.com/api/v1/ticker/24hr?symbol=ETHBTC"),
    Tickers(CoinPair("eth", "usd"), "https://www.binance.com/api/v1/ticker/24hr?symbol=ETHUSDT"),
    Tickers(CoinPair("btc", "usd"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BTCUSDT"),
    Tickers(CoinPair("ltc", "btc"), "https://www.binance.com/api/v1/ticker/24hr?symbol=LTCBTC"),
    Tickers(CoinPair("bnb", "btc"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BNBBTC"),
    Tickers(CoinPair("neo", "btc"), "https://www.binance.com/api/v1/ticker/24hr?symbol=NEOBTC"),
    Tickers(CoinPair("bcc", "btc"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BCCBTC"),
    Tickers(CoinPair("eos", "eth"), "https://www.binance.com/api/v1/ticker/24hr?symbol=EOSETH"),
    Tickers(CoinPair("snt", "eth"), "https://www.binance.com/api/v1/ticker/24hr?symbol=SNTETH"),
    Tickers(CoinPair("bnb", "eth"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BNBETH"),
    Tickers(CoinPair("qtum", "eth"), "https://www.binance.com/api/v1/ticker/24hr?symbol=QTUMETH")
  )

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, String]]
        val volume = data.getOrElse("volume", "0.0")
        val lastPrice = data.getOrElse("lastPrice", "0.0")

        CoinTicker("binance", ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Binance {

  def apply(hc: AsyncHttpClient): Binance = new Binance() {

    override val httpClient: AsyncHttpClient = hc

  }

}

