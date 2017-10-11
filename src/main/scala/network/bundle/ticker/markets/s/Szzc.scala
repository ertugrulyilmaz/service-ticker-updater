package network.bundle.ticker.markets.s

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. volume calculated by day, should divide by 1E8.
  * @see https://szzc.com/api/public/
  * @see https://szzc.com/api/trader/
  * @see https://szzc.com/api/public/tickers
  * @see https://szzc.com/api/public/ticker/BTCCNY
  */
trait Szzc extends BaseMarket {

  val url = "https://szzc.com/api/public/tickers"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, List[Map[String, Any]]]]("result")
        .map { pair =>
          val asset = pair("market").toString.split("CNY").head.toLowerCase
          val volume = BigDecimal(pair("vol24h").toString) * BigDecimal(0.00000001)
          val lastPrice = BigDecimal(pair("last24h").toString) * BigDecimal(0.00000001)

          CoinTicker("szzc", CoinPair(asset, "cny"), volume, lastPrice)
        }
    }
  }

}

object Szzc {

  def apply(hc: AsyncHttpClient): Szzc = new Szzc() {

    override val httpClient: AsyncHttpClient = hc

  }

}