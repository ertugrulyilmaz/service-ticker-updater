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
  * @see https://cex.io/rest-api#tickers-all
  * @see https://cex.io/api/tickers/USD/EUR/RUB/BTC
  */
trait Cex extends BaseMarket {

  val url = "https://cex.io/api/tickers/USD/EUR/RUB/BTC"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, List[Map[String, String]]]]("data").map { pair =>
        val assetCurrency = pair("pair").toString.toLowerCase.split(":")
        val asset = assetCurrency.head
        val currency = assetCurrency.last
        val volume = pair("volume").toString
        val lastPrice = pair("last").toString

        CoinTicker("cex", CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Cex {

  def apply(hc: AsyncHttpClient): Cex = new Cex() {

    override val httpClient: AsyncHttpClient = hc

  }

}