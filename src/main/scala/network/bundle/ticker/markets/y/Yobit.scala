package network.bundle.ticker.markets.y

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below.
  * @see https://www.yobit.net/en/api/
  * @see https://yobit.net/en/coinsinfo/
  * @see https://yobit.net/api/3/info
  * @see https://yobit.net/api/3/ticker/ltc_btc
  */
trait Yobit extends BaseMarket {

  val url = "https://yobit.net/api/3/info"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val res = HttpClientFactory.completedGet(httpClient, url)
    val vals = parse(res.getResponseBody)
      .values.asInstanceOf[Map[String, Map[String, Any]]]("pairs")
      .keys
      .filter(_.endsWith("_btc"))
      .toList

    Future.traverse(vals) { pairs =>
      HttpClientFactory.get(httpClient, s"https://yobit.net/api/3/ticker/$pairs").map { resPair =>
        val data = parse(resPair.getResponseBody)
          .values.asInstanceOf[Map[String, Map[String, Any]]](pairs)
        val assetCurrency = pairs.split("_")
        val asset = reformatCurrencyName(assetCurrency.head)
        val currency = reformatCurrencyName(assetCurrency.last)
        val volume = data("vol_cur").toString
        val lastPrice = data("last").toString

        CoinTicker("yobit", CoinPair(asset, currency, pairs), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Yobit {

  def apply(hc: AsyncHttpClient): Yobit = new Yobit() {

    override val httpClient: AsyncHttpClient = hc

  }

}