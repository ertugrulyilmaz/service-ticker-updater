package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://bter.com/api2
  * @see http://data.bter.com/api2/1/marketlist
  * @see http://data.bter.com/api2/1/pairs
  * @see http://data.bter.com/api2/1/tickers
  * @see http://data.bter.com/api2/1/ticker/btc_cny
  */
trait Bter extends BaseMarket {

  val url = "http://data.bter.com/api2/1/tickers"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, Map[String, Any]]]
        .map { data =>
          val assetCurrency = data._1.split("_")
          val asset = assetCurrency.head
          val currency = assetCurrency.last
          val lastPrice = data._2("last").toString
          val volume = data._2("quoteVolume").toString

          CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
        }.to[immutable.Seq]
    }
  }

}

object Bter {

  def apply(hc: AsyncHttpClient): Bter = new Bter() {

    override val httpClient: AsyncHttpClient = hc

  }

}