package network.bundle.ticker.markets.e

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note there is no documentation
  * @see https://etherdelta.github.io/
  * @see https://cache1.etherdelta.com/nonce/d13121ea535baa4a4e9113a23fee40e19cff517d92039f028a3ff9d69ea0a679/returnTicker
  */
trait EtherDelta extends BaseMarket {

  val url = "https://cache1.etherdelta.com/nonce/d13121ea535baa4a4e9113a23fee40e19cff517d92039f028a3ff9d69ea0a679/returnTicker"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, Map[String, Any]]].map { pair =>
        val assetCurrency = pair._1.toString.split("_")
        val asset = reformatCurrencyName(assetCurrency.last.toLowerCase)
        val currency = reformatCurrencyName(assetCurrency.head.toLowerCase)
        val volume = pair._2("quoteVolume").toString
        val lastPrice = pair._2("last").toString

        CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }.to[immutable.Seq]
    }
  }

}

object EtherDelta {

  def apply(hc: AsyncHttpClient): EtherDelta = new EtherDelta() {

    override val httpClient: AsyncHttpClient = hc

  }

}