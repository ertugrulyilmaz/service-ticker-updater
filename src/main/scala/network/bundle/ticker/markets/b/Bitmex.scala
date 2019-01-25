package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentaion page is below
  * @see https://www.bitmex.com/api/explorer/
  * @see https://www.bitmex.com/api/v1/stats
  */
trait Bitmex extends BaseMarket {

  val url = "https://www.bitmex.com/api/v1/stats"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody).values
        .asInstanceOf[List[Map[String, Any]]]
        .map { data =>
          val asset = data("rootSymbol").toString.toLowerCase
          val currency = data("currency").toString.toLowerCase
          val volume = data("volume24h").asInstanceOf[BigInt]
          val lastPrice = data("openValue").asInstanceOf[BigInt]

          (asset, currency, volume, lastPrice)
        }
        .filter(data => data._3 != null)
        .filter(data => data._3 > 0)
        .map { data =>
          val asset = data._1
          val currency = data._2
          val volume = data._3
          val lastPrice = data._4

          CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice) * BigDecimal(0.00000001))
        }
    }
  }

}

object Bitmex {

  def apply(hc: AsyncHttpClient): Bitmex = new Bitmex() {

    override val httpClient: AsyncHttpClient = hc

  }

}