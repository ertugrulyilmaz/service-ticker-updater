package network.bundle.ticker.markets.c

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below.
  * @see https://www.cryptopia.co.nz/Forum/Category/45
  * @see https://www.cryptopia.co.nz/api/GetMarkets
  */
trait Cryptopia extends BaseMarket {

  val url = "https://www.cryptopia.co.nz/api/GetMarkets"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, List[Map[String, Any]]]]("Data").map { pair =>
        val assetCurrency = pair("Label").toString.toLowerCase.split("/")
        val asset = assetCurrency.head
        val currency = assetCurrency.last
        val volume = pair("Volume").toString
        val lastPrice = pair("LastPrice").toString

        CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Cryptopia {

  def apply(hc: AsyncHttpClient): Cryptopia = new Cryptopia() {

    override val httpClient: AsyncHttpClient = hc

  }

}