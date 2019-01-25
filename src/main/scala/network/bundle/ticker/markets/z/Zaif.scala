package network.bundle.ticker.markets.z

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below.
  * @see http://techbureau-api-document.readthedocs.io/ja/latest/index.html
  * @see https://api.zaif.jp/api/1/currencies/all
  * @see https://api.zaif.jp/api/1/currency_pairs/all
  * @see https://api.zaif.jp/api/1/ticker/btc_jpy
  */
trait Zaif extends BaseMarket {

  val pairUrl = "https://api.zaif.jp/api/1/currency_pairs/all"
  val tickerUrl = "https://api.zaif.jp/api/1/ticker"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val res = HttpClientFactory.completedGet(httpClient, pairUrl)
    val vals = parse(res.getResponseBody)
      .values
      .asInstanceOf[List[Map[String, String]]]

    Future.traverse(vals) { pair =>
      val currencyPair = pair("currency_pair").split("_")
      val asset = currencyPair.head
      val currency = currencyPair.last

      HttpClientFactory.get(httpClient, s"$tickerUrl/${asset}_${currency}").map { res2 =>
        val data = parse(res2.getResponseBody).values.asInstanceOf[Map[String, Double]]
        val volume = data.getOrElse("volume", 0.0)
        val lastPrice = data.getOrElse("last", 0.0)

        CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Zaif {

  def apply(hc: AsyncHttpClient): Zaif = new Zaif() {

    override val httpClient: AsyncHttpClient = hc

  }

}