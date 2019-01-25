package network.bundle.ticker.markets.l

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below.
  * @see https://www.livecoin.net/api?lang=en
  * @see https://api.livecoin.net/exchange/ticker
  * @see https://api.livecoin.net/exchange/ticker?currencyPair=BTC/USD
  */
trait Livecoin extends BaseMarket {

  val url = "https://api.livecoin.net/exchange/ticker"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody).values.asInstanceOf[List[Map[String, Any]]]
        .map { pair =>
          val assetCurrency = pair("symbol").toString.split("/")
          val asset = assetCurrency.head.toLowerCase
          val currency = assetCurrency.last.toLowerCase
          val volume = pair("volume").toString
          val lastPrice = pair("last").toString

          CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
        }
    }
  }

}

object Livecoin {

  def apply(hc: AsyncHttpClient): Livecoin = new Livecoin() {

    override val httpClient: AsyncHttpClient = hc

  }

}