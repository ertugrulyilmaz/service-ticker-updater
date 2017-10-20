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
    Tickers(CoinPair("eth", "btc", "ETHBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=ETHBTC"),
    Tickers(CoinPair("eth", "usd", "ETHUSDT"), "https://www.binance.com/api/v1/ticker/24hr?symbol=ETHUSDT"),
    Tickers(CoinPair("btc", "usd", "BTCUSDT"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BTCUSDT"),
    Tickers(CoinPair("ltc", "btc", "LTCBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=LTCBTC"),
    Tickers(CoinPair("bnb", "btc", "BNBBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BNBBTC"),
    Tickers(CoinPair("neo", "btc", "NEOBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=NEOBTC"),
    Tickers(CoinPair("bcc", "btc", "BCCBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BCCBTC"),
    Tickers(CoinPair("eos", "eth", "EOSETH"), "https://www.binance.com/api/v1/ticker/24hr?symbol=EOSETH"),
    Tickers(CoinPair("snt", "eth", "SNTETH"), "https://www.binance.com/api/v1/ticker/24hr?symbol=SNTETH"),
    Tickers(CoinPair("bnb", "eth", "BNBETH"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BNBETH"),
    Tickers(CoinPair("qtum", "eth", "QTUMETH"), "https://www.binance.com/api/v1/ticker/24hr?symbol=QTUMETH"),
    Tickers(CoinPair("eth", "btc", "ETHBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=ETHBTC"),
    Tickers(CoinPair("neo", "btc", "NEOBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=NEOBTC"),
    Tickers(CoinPair("wtc", "btc", "WTCBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=WTCBTC"),
    Tickers(CoinPair("bnb", "btc", "BNBBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BNBBTC"),
    Tickers(CoinPair("link", "btc", "LINKBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=LINKBTC"),
    Tickers(CoinPair("mco", "btc", "MCOBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=MCOBTC"),
    Tickers(CoinPair("qtum", "btc", "QTUMBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=QTUMBTC"),
    Tickers(CoinPair("omg", "btc", "OMGBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=OMGBTC"),
    Tickers(CoinPair("ltc", "btc", "LTCBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=LTCBTC"),
    Tickers(CoinPair("knc", "btc", "KNCBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=KNCBTC"),
    Tickers(CoinPair("ctr", "btc", "CTRBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=CTRBTC"),
    Tickers(CoinPair("iota", "btc", "IOTABTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=IOTABTC"),
    Tickers(CoinPair("eos", "btc", "EOSBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=EOSBTC"),
    Tickers(CoinPair("salt", "btc", "SALTBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=SALTBTC"),
    Tickers(CoinPair("strat", "btc", "STRATBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=STRATBTC"),
    Tickers(CoinPair("bcc", "btc", "BCCBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BCCBTC"),
    Tickers(CoinPair("snm", "btc", "SNMBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=SNMBTC"),
    Tickers(CoinPair("sub", "btc", "SUBBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=SUBBTC"),
    Tickers(CoinPair("mda", "btc", "MDABTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=MDABTC"),
    Tickers(CoinPair("gas", "btc", "GASBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=GASBTC"),
    Tickers(CoinPair("fun", "btc", "FUNBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=FUNBTC"),
    Tickers(CoinPair("sngls", "btc", "SNGLSBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=SNGLSBTC"),
    Tickers(CoinPair("bqx", "btc", "BQXBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=BQXBTC"),
    Tickers(CoinPair("zrx", "btc", "ZRXBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=ZRXBTC"),
    Tickers(CoinPair("xvg", "btc", "XVGBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=XVGBTC"),
    Tickers(CoinPair("mtl", "btc", "MTLBTC"), "https://www.binance.com/api/v1/ticker/24hr?symbol=MTLBTC")
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

