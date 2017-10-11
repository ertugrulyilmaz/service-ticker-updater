package network.bundle.ticker.markets.q

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://developers.quoine.com
  * @see https://api.quoine.com/products
  * @see https://api.quoine.com/products/:id
  */
trait Quoine extends BaseMarket {

  val url = "https://api.quoine.com/products"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[List[Map[String, Any]]]
        .map { pair =>
          val asset = pair("base_currency").toString.toLowerCase
          val currency = pair("currency").toString.toLowerCase
          val volume = pair("volume_24h").toString
          val lastPrice = pair("last_price_24h").toString

          CoinTicker("quoine", CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
        }
    }
  }

}

object Quoine {

  def apply(hc: AsyncHttpClient): Quoine = new Quoine() {

    override val httpClient: AsyncHttpClient = hc

  }

}