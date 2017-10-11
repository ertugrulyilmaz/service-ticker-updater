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
  val coinPairs: immutable.Seq[CoinPair] = immutable.Seq(CoinPair("btc", "usd"), CoinPair("ltc", "usd"), CoinPair("ltc", "btc"),
    CoinPair("eth", "usd"), CoinPair("eth", "btc"), CoinPair("etc", "btc"), CoinPair("etc", "usd"),
    CoinPair("rrt", "usd"), CoinPair("rrt", "btc"), CoinPair("zec", "usd"), CoinPair("zec", "btc"),
    CoinPair("xmr", "usd"), CoinPair("xmr", "btc"), CoinPair("dsh", "usd"), CoinPair("dsh", "btc"),
    CoinPair("bcc", "btc"), CoinPair("bcu", "btc"), CoinPair("bcc", "usd"), CoinPair("bcu", "usd"),
    CoinPair("xrp", "usd"), CoinPair("xrp", "btc"), CoinPair("iot", "usd"), CoinPair("iot", "btc"),
    CoinPair("iot", "eth"), CoinPair("eos", "usd"), CoinPair("eos", "btc"), CoinPair("eos", "eth"),
    CoinPair("san", "usd"), CoinPair("san", "btc"), CoinPair("san", "eth"), CoinPair("omg", "usd"),
    CoinPair("omg", "btc"), CoinPair("omg", "eth"), CoinPair("bch", "usd"), CoinPair("bch", "btc"),
    CoinPair("bch", "eth"))

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

    override val httpClient: AsyncHttpClient = hc

  }

}