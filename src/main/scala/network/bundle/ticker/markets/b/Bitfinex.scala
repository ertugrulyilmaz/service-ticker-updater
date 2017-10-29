package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note Market Api Informations
  * @see https://docs.bitfinex.com
  * @see https://bitfinex.readme.io/v2/docs/rest-public
  * @see https://api.bitfinex.com/v1/symbols
  * @see https://api.bitfinex.com/v1/stats/:symbol
  * @see https://api.bitfinex.com/v1/pubticker/:symbol
  *
  **/
trait Bitfinex extends BaseMarket {

  val baseUrl: String = "https://api.bitfinex.com/v1/pubticker"
  val coinPairs: immutable.Seq[CoinPair] = immutable.Seq(
    CoinPair("btc", "usd", "btcusd"),
    CoinPair("ltc", "usd", "ltcusd"), CoinPair("ltc", "btc", "ltcbtc"),
    CoinPair("eth", "usd", "ethusd"), CoinPair("eth", "btc", "ethbtc"),
    CoinPair("etc", "btc", "etcbtc"), CoinPair("etc", "usd", "etcusd"),
    CoinPair("rrt", "usd", "rrtusd"), CoinPair("rrt", "btc", "rrtbtc"),
    CoinPair("zec", "usd", "zecusd"), CoinPair("zec", "btc", "zecbtc"),
    CoinPair("xmr", "usd", "xmrusd"), CoinPair("xmr", "btc", "xmrbtc"),
    CoinPair("dsh", "usd", "dshusd"), CoinPair("dsh", "btc", "dshbtc"),
    CoinPair("bcc", "btc", "bccbtc"), CoinPair("bcc", "usd", "bccusd"),
    CoinPair("bcu", "btc", "bcubtc"), CoinPair("bcu", "usd", "bcuusd"),
    CoinPair("xrp", "usd", "xrpusd"), CoinPair("xrp", "btc", "xrpbtc"),
    CoinPair("iot", "usd", "iotusd"), CoinPair("iot", "btc", "iotbtc"), CoinPair("iot", "eth", "ioteth"),
    CoinPair("eos", "usd", "eosusd"), CoinPair("eos", "btc", "eosbtc"), CoinPair("eos", "eth", "eoseth"),
    CoinPair("san", "usd", "sanusd"), CoinPair("san", "btc", "sanbtc"), CoinPair("san", "eth", "saneth"),
    CoinPair("omg", "usd", "omgusd"), CoinPair("omg", "btc", "omgbtc"), CoinPair("omg", "eth", "omgeth"),
    CoinPair("bch", "usd", "bchusd"), CoinPair("bch", "btc", "bchbtc"), CoinPair("bch", "eth", "bcheth"),
    CoinPair("neo", "usd", "neousd"), CoinPair("neo", "btc", "neobtc"), CoinPair("neo", "eth", "neoeth"),
    CoinPair("etp", "usd", "etpusd"), CoinPair("etp", "btc", "etpbtc"), CoinPair("etp", "eth", "etpeth"),
    CoinPair("qtm", "usd", "qtmusd"), CoinPair("qtm", "btc", "qtmbtc"), CoinPair("qtm", "eth", "qtmeth"),
    CoinPair("avt", "usd", "avtusd"), CoinPair("avt", "btc", "avtbtc"), CoinPair("avt", "eth", "avteth")
  ).filter(_.currency == "btc")

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(coinPairs) { coinPair =>
      val url = s"$baseUrl/${coinPair.asset}${coinPair.currency}"
      HttpClientFactory.get(httpClient, url).map { response =>
        val data = parse(response.getResponseBody).values.asInstanceOf[Map[String, String]]
        val volume = data("volume")
        val lastPrice = data("last_price")
        CoinTicker("bitfinex", coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Bitfinex {

  def apply(hc: AsyncHttpClient): Bitfinex = new Bitfinex() {

    override val id = 2L
    override val httpClient: AsyncHttpClient = hc

  }

}